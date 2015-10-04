# rpsl4j-parser #
A Routing Policy Specification Language parsing library forked from RIPE-NNC's [whois](https://github.com/RIPE-NCC/whois/) project.
The library contains the subset of classes required to parse and represent RPSL objects, along with added functionality to facilitate inspection of complex attributes.

## Building and deploying ##
```
$ mvn package
```

_for deployment instructions, refer to the rpsl4j-generator_

## Usage ##
### Parsing string into RPSLObject set ###
```java
String rpslDocumentString = "...";
Set<RpslObject> objectSet = new HashSet<>():
RpslObjectStringReader rpslReader = new RpslObjectStringReader(rpslDocumentString);

for(String objStr : rpslReader)
    objectSet.add(RpslObject.parse(objStr));
```

You can also use `RpslObjectFileReader` or the more general `RpslObjectStreamReader` if they are a better fit for your project.

### Read the attributes of the RPSL object ###
```java
RpslObject routeSetExample = RpslObject.parse("route: 1.1.1.0/24\n
origin: AS1\n mnt-by MNT-EXAMPLE # Need to change owner\n");

// The value of the class attribute is retrieved as follows
RpslAttribute routePrefixAttr = routeSetExample.getTypeAttribute();

// The value of a specific attribute can be retrieved using the AttributeType enumerator
RpslAttribute originASAttr = routeSetExample.findAttribute(AttributeType.ORIGIN);

// Many attribute types have corresponding classes that provide useful encapsulation and functionality.
// Refer to the net.ripe.db.whois.common.rpsl.attrs package
//
AutNum originAS = AutNum.parse(originAsAttr.getCleanValue());
AddressPrefixRange routePrefix = AddressPrefixRange.parse(routePrefixAttr.getCleanValue());

// If you want to full text of an attribute (including comments etc), use RpslAttribute#getValue() instead of RpslAttribute#getCleanValue()
System.out.println(routeSetExample.findAttribute(AttributeType.MNT_BY).getValue());
```

### Using the generated lexers to inspect complex attributes ###
When rpsl4j-parser is compiled, parsers and lexers are generated from bison and jflex sources for many RPSL attributes.
These can be found in the `net.ripe.db.whois.common.generated` package and are located in the `target/generated-sources/jflex` directory.
These are primarily used for validity checks but, in combination with the `AttributeLexerWrapper` class, can be used to inspect supported attributes more deeply.

The simplest interface to this functionality is the `RpslAttribute#getTokenList` method.
This method will either return a list of syntax tokens (describd below) or an empty list, so it's important to check which attributes lexer's exist for before calling it.

The method returns the Java type `List<Pair<String, List<String>>>`, which represents the type `[(TOKEN_TYPE, [TOKEN_VALUES])]`. An example of this process follows:

```java
RpslAttribute importAttribute = RpslAttrbute.parse(AttributeType.IMPORT, "import: from AS2 accept AS1, 1.2.3.4/5");

// Generate the token list for the attribute
List<Pair<String, List<String>>> importTokenList = importAttribtue.getTokenList();

System.out.println(importTokenList); // prints "[(from, [AS2]), (accept, [AS1, 1.2.3.4/5])]""

// Iterate through the tokens
for(Pair<String, List<String>> tokenPair : exportAttribute.getTokenList()) {
    String tokenType = tokenPair.getLeft();
    List<String> values = tokenPair.getRight();

    if(tokenType.equals("from"))
        setOrigins(values);
    else if(tokenType.equals("accept"))
        setIncoming(values);
}
```

`AttributeLexerWrapper` works by generating a table of lexer states and their corresponding token names via reflection, then running the lexer over an attribute; recording tokens and runs of values.
Unfortunately there is no easy way of predetermining the structure of a token list without thoroughly studying the attributes corresponding lexer.
The best approach at present is by experiment and testing.

_refer to the rpsl4j-generator library for more complex uses of AttributeLexerWrapper._

## License ##
Excluding code already licensed by RIPE under the BSD license, the source code is licensed under the GNU Affero General Public License.
