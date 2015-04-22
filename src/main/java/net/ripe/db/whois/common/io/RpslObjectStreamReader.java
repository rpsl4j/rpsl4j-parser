/*
 * Copyright (c) 2013 RIPE NCC, 2015 Benjamin Roberts
 * All rights reserved.
 */

package net.ripe.db.whois.common.io;

import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Iterator;

public class RpslObjectStreamReader implements Iterable<String> {
	protected InputStream inputStream;
	
	public RpslObjectStreamReader() {}
	
    public RpslObjectStreamReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }


    public Iterator<String> iterator() {
    	if(inputStream == null) {
    		return Collections.emptyIterator();
    	}
        return new StringIterator(inputStream);
    }

    protected class StringIterator implements Iterator<String> {
        private final BufferedReader bufferedReader;
        private String nextObject;

        public StringIterator(InputStream inputStream) {
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        }

        @Override
        public boolean hasNext() {
            if (nextObject == null) {
                nextObject = next();
            }
            return nextObject != null;
        }

        @Override
        public String next() {
            if (nextObject != null) {
                String ret = nextObject;
                nextObject = null;
                return ret;
            }

            try {
                String result;

                do {
                    String line;
                    final StringBuilder partialObject = new StringBuilder(1024);

                    while ((line = bufferedReader.readLine()) != null) {
                        if (StringUtils.isBlank(line)) {
                            break;
                        } else {
                            if (line.charAt(0) != '#' && line.charAt(0) != '%') {
                                partialObject.append(line).append('\n');
                            }
                        }
                    }

                    if (line == null && partialObject.length() == 0) {
                        return null; // terminator
                    }

                    result = partialObject.toString();

                } while (StringUtils.isBlank(result));

                return result;
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
