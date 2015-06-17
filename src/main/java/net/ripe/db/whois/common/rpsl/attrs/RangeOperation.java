package net.ripe.db.whois.common.rpsl.attrs;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RangeOperation {
    private static final Pattern RANGE_OPERATION_PATTERN = Pattern.compile("^\\^(?:[+-]|(\\d+)(?:\\-(\\d+))?)$");

    private final Integer n;
    private final Integer m;

    public static RangeOperation parse(final String value, final int prefixLength, final int maxRange) {
        if (StringUtils.isEmpty(value)) {
            return new RangeOperation(prefixLength, prefixLength);
        }

        final Matcher matcher = RANGE_OPERATION_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new AttributeParseException("Invalid range operation", value);
        }

        //more specifics modes (^- and ^+): specify addresses more specific than the current, ^- excluding it, ^+ including it.
        //eg 1.1.1.1/8^- will also match 1.2.3.4/24, because the mask is *higher*, and the network portion matched by the original address mask (/8), is the same (1.x.x.x)
        
        if (value.startsWith("^-")) { //exclusive more specifics operator
            return new RangeOperation(prefixLength + 1, maxRange); //any prefix more specific than the current (so +1) and within the max length of the address type (eg 32 for ipv4)
        }

        if (value.startsWith("^+")) { //inclusive more specifics operator
            return new RangeOperation(prefixLength, maxRange);
        }

        
        //move on to length specified modes (^n and ^n-m)
        
        final Integer n = Integer.parseInt(matcher.group(1));
        if (n < prefixLength) {
            throw new AttributeParseException("n cannot be smaller than prefix length", value);
        }

        if (n > maxRange) {
            throw new AttributeParseException("n cannot be larger than max range" + n, value);
        }

        //look for ^n-m mode, and return with ^n mode if we don't find m.
        final Integer m = matcher.group(2) == null ? null : Integer.parseInt(matcher.group(2));
        if (m == null) {
            return new RangeOperation(n, n);
        }

        //validate m and return.
        
        if (m > maxRange) {
            throw new AttributeParseException("Invalid m: " + m, value);
        }

        if (n > m) {
            throw new AttributeParseException("Too large n: " + n, value);
        }

        return new RangeOperation(n, m);
    }

    private RangeOperation(final Integer n, final Integer m) {
        this.n = n;
        this.m = m;
    }

    public Integer getN() {
        return n;
    }

    public Integer getM() {
        return m;
    }
    
    @Override
    public int hashCode() { //TODO: untested
    	return toString().hashCode();
    }
    
    @Override
    public String toString() { //TODO: untested
    	return "RangeOperation [n:" + n + " m:" + m + "]";
    }
    
    @Override
    public boolean equals(final Object o) { //TODO: untested
    	if(o == this)
    		return true;
    	if(o == null || !(o instanceof RangeOperation))
    		return false;
    	else {
    		final RangeOperation that = (RangeOperation) o;
    		return m.equals(that.m) && n.equals(that.n);
    	}
    }
}
