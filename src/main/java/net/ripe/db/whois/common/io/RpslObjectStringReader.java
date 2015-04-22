/*
 * Copyright (c) 2015 Benjamin Roberts, Nathan Kelly, Andrew Maxwell
 * All rights reserved.
 */

package net.ripe.db.whois.common.io;

import java.io.StringReader;

import org.apache.commons.io.input.ReaderInputStream;

/**
 * RPSL Object reader for plain Java Strings
 * @author Benjamin George Roberts
 */
public class RpslObjectStringReader extends RpslObjectStreamReader {
	public RpslObjectStringReader(String rpslString) {
		StringReader reader = new StringReader(rpslString);
		inputStream = new ReaderInputStream(reader);
	}
}
