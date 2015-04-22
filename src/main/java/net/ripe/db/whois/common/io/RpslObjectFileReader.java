/*
 * Copyright (c) 2013 RIPE NCC, 2015 Benjamin Roberts
 * All rights reserved.
 */

package net.ripe.db.whois.common.io;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class RpslObjectFileReader extends RpslObjectStreamReader {
    
    public RpslObjectFileReader(final String fileName) {
        try {
        	inputStream = new FileInputStream(fileName);
            if (fileName.endsWith(".gz")) {
            	inputStream = new GZIPInputStream(inputStream);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(fileName, e);
        }
    }
}
