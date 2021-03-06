/*
 * Copyright (c) 2015 Benjamin Roberts, Nathan Kelly, Andrew Maxwell
 * All rights reserved.
 */

package net.ripe.db.whois.common.rpsl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.ripe.db.whois.common.domain.CIString;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * This class provides a wrapper for the {@link AttributeLexer}s and associated parsers generated by the RIPE RPSL package.
 * It runs the lexer in order to generate a usable representation, "[(keyword, [values])", of the parsed attribute.
 * @author Benjamin George Roberts
 */
public class AttributeLexerWrapper {
	private static final String classLocation = "net.ripe.db.whois.common.generated.";
	private static Map<String, Map<Integer, String>> stateTableCache = new HashMap<String, Map<Integer, String>>();
	private static Pattern statePattern = Pattern.compile("((TKN)|(OP)|(KEYW))_[A-Z0-9]+");
	
	private String attributeType;
	private AttributeLexer lexer;
	private Map<Integer, String> stateTable;
	
	/**
	 * Construct the lexer for the provided field
	 * @param fieldName The name of the attribute/field to instantiate the lexer for. Example: import, filter.
	 * @throws ClassNotFoundException Thrown if the attribute does not have an assocaited parser & lexer, or if the classes are uninstantiable.
	 */
	public AttributeLexerWrapper(String fieldName) throws ClassNotFoundException {
		attributeType = WordUtils.capitalize(fieldName);
		lexer = loadLexerInstance(attributeType);
		stateTable = generateStateTable(attributeType);
	}
	
	/**
	 * @see AttributeLexerWrapper#Lexer(String, Reader)
	 * @param in source of text to parse. Will be wrapped by a reader in {@link AttributeLexerWrapper#Lexer(String, Reader)}
	 */
	public AttributeLexerWrapper(String fieldName, InputStream in) throws ClassNotFoundException {
	}
	
	/**
	 * Locates and instantiates an AttributeLexer instance from the JVM's classpath
	 * @param attributeType the attribute to load a lexer for. Example: import, filter
	 * @param in Reader to instantiate the lexer with
	 * @return The an instance of the lexer of the provided attributeType
	 * @throws ClassNotFoundException Thrown if the attribute does not have an associated lexer or if the class is uninstantiable.
	 */
	private static AttributeLexer loadLexerInstance(String attributeType) throws ClassNotFoundException {
		//Attempt to load the lexer class from the classpath. Will throw ClassNotFoundException on fail
		String lexerClassName = classLocation + attributeType + "Lexer";
		@SuppressWarnings("unchecked")
		Class<AttributeLexer> lexerClass =  (Class<AttributeLexer>) Class.forName(lexerClassName);
		
		//This section instantiates the lexer, lots of things can go wrong which we throw upstream as a modified ClassNotFoundException
		try {
			Constructor<AttributeLexer> lexerConstructor = lexerClass.getConstructor(Reader.class);
			Reader nullReader = null;
			return lexerConstructor.newInstance(nullReader); //Pass null now and give a real input when parse is called
		} catch (Exception e) {
			throw (new ClassNotFoundException(lexerClass.getName() + " is not a valid AttributeParser", e));
		}
	}
	
	/**
	 * Generates a map of state-numbers to state names from the parser of the provided attribute type.
	 * Successfully generated maps are cached.
	 * @param attributeType the attribute to load the state table of.
	 * @return Map of state to name
	 * @throws ClassNotFoundException Thrown if the attribute does not have an associated parser.
	 */
	private static Map<Integer, String> generateStateTable(String attributeType) throws ClassNotFoundException {
		//Check if we've already cached a state table for this parser
		if(stateTableCache.containsKey(attributeType))
			return stateTableCache.get(attributeType);
		
		//Attempt to load the parser class from the classpath. Will throw ClassNotFoundException on fail
		String parserClassName = classLocation + attributeType + "Parser";
		@SuppressWarnings("unchecked")
		Class<AttributeLexer> parserClass =  (Class<AttributeLexer>) Class.forName(parserClassName);
		
		//Build a new stateTable by finding state variables in the parser class
		Map<Integer, String> stateTable = new HashMap<Integer, String>();
		
		for(Field field : parserClass.getDeclaredFields()) {
			//State fields have the signature PUBLIC FINAL STATIC short {KEYW}|{OP}|{TKN}_\w.
			int modifierMask = Modifier.PUBLIC | Modifier.FINAL | Modifier.STATIC;
			
			if(field.getModifiers() == modifierMask 
					&& field.getType() == short.class
					&& statePattern.matcher(field.getName()).matches()) {
				
				//This shouldn't fail as we've already typechecked, but we skip on failure.
				try {
					stateTable.put(new Integer(field.getShort(null)), field.getName());
				} catch (IllegalArgumentException | IllegalAccessException e) {
					System.err.println("Failed to extract value for state table: " + field.getName());
					e.printStackTrace();
				}
			}
		}
		//Cache and return new table
		stateTableCache.put(attributeType, stateTable);
		return stateTable;
	}
		
	/**
	 * @see AttributeLexerWrapper#parse(Reader)
	 * @param in source of text to parse. Will be wrapped by a reader in {@link AttributeLexerWrapper#parse(Reader)}
	 */
	public List<Pair<String, List<String>>> parse(InputStream in) throws IOException {
		return parse(new InputStreamReader(in));
	}
	
	/**
	 * Runs the lexer over the input text and builds a more usable representation.
	 * This representation is structured as a List of {@link Pair}s. 
	 * The left value is the last keyword (or combination of keywords) encountered, 
	 * whilst the right value is a (not null) list of captured tokens and operators.
	 * If the first item to be encountered is a token (ie ifaddr attributes), the left value
	 * is the name of the token (ie DNS).
	 * 
	 * All keywords and operations (and/or/not) are lowercase
	 * 
	 * Example:
	 * the string "import: from AS2 accept AS1, 1.2.3.4/5" would be returned as
	 * "[(from, [AS2]), (accept, [AS1, 1.2.3.4/5])]"
	 * 
	 * 
 	 * @param in Source of the text to parse
	 * @return representation of the parsed text
	 * @throws IOException thrown if an error occurs reading the stream
	 */
	public List<Pair<String, List<String>>> parse(Reader in) throws IOException {
		lexer.yyreset(in);
		
		int lexerState = lexer.yylex(); //take initial state
		boolean capturingKeyword = false; //concat strings of keywords
		String previousKeyWord = null;
		List<String> previousTokenList = null;
		List<Pair<String, List<String>>> contextMap = new LinkedList<Pair<String, List<String>>>();
		
		while(lexerState > 2 && lexerState != 256) { //256 is parser error code, [-1,2] are parser error (including EOF)
			String stateName;
			
			//Some characters return unnamed states, print them as errors and continue
			if (!stateTable.containsKey(lexerState)) {
				lexerState = lexer.yylex();
				continue;
			} else {
				stateName = stateTable.get(lexerState);
			}
			
			//Capture the keywords or store the tokens
			if(stateName.startsWith("KEYW_")) {
				//Remove KEYW_ from the statename
				stateName = stateName.substring(5).toLowerCase();
				
				//Start a new keyword concat if we'ce just come off a token or this is first run
				if(!capturingKeyword || previousKeyWord == null) {
					previousKeyWord = stateName;
					previousTokenList = null;
					capturingKeyword = true;
				} else {
					previousKeyWord += "&" + stateName;
				}
				lexerState = lexer.yylex();
				continue;
			} else {
				capturingKeyword = false;
				//If the first thing we encounter in the attribute is a token, make an entry for it using its state name
				if(previousKeyWord == null)
					previousKeyWord = stateName.substring(4).toLowerCase(); //remove TKN_
					
				//append the token
				if(lexer.yylength() > 0) {
					//Add an entry into the contextmap
					if(previousTokenList == null) {
						previousTokenList = new LinkedList<String>();
						contextMap.add(Pair.of(previousKeyWord, previousTokenList));
					}
					String text = lexer.yytext();
					
					//Skip adding the attribute key (ie import in import: ...)
					if(text.toLowerCase().equals(attributeType.toLowerCase())) {
						lexerState = lexer.yylex(); 
						continue;
					}
					
					//Remove OP_ from operations
					if(text.startsWith("OP_")) 
						text = text.substring(3).toLowerCase();
					
					//Append to token list
					previousTokenList.add(text);
				} 
			} 
			//Advance to next state
			lexerState = lexer.yylex();
		}
		
		//Remove empty entires from context map
		List<Pair<String, List<String>>> emptyEntries = new LinkedList<Pair<String, List<String>>>();
		for(Pair<String, List<String>> entry : contextMap) {
			if(entry.getRight().size() == 0)
				emptyEntries.add(entry);
		}
		contextMap.removeAll(emptyEntries);
		
		//Return map
		return contextMap;
	}
	
	/**
	 * Parse an {@link RpslAttribute} object
	 * If the lexer class is not found for the provided attribute, a simple token list is built
	 * from the attribute ie: [(Attr-Type, [cleanValues...])]
	 * @see AttributeLexerWrapper#parse(Reader)
	 * @return representation of the parsed text or empty list.
	 */
	public static List<Pair<String, List<String>>> parse(RpslAttribute attr) throws ClassNotFoundException{
		try {
			AttributeLexerWrapper lexer = new AttributeLexerWrapper(attr.getType().getName());
			return lexer.parse(new StringReader(attr.toString()));
 
		} catch (IOException | ClassNotFoundException e) {
			//System.err.println("IO error parsing attribute: " + attr.toString());
			if(attr.getCleanValues().size() > 1) {
				//Case for multiple values
				List<String> values = new LinkedList<String>();
				for(CIString val : attr.getCleanValues())
					values.add(val.toString());
				
				return Arrays.asList(Pair.of(attr.getType().getName(), values));
			} else if (attr.getCleanValues().size() == 1) {
				//One value
				return Arrays.asList(Pair.of(attr.getType().getName(), Arrays.asList(attr.getCleanValue().toString())));
			} else {
				//No value
				return Arrays.asList();
			}
		}
	}
}
