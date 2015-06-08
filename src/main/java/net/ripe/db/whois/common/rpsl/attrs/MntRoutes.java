package net.ripe.db.whois.common.rpsl.attrs;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.ripe.db.whois.common.domain.CIString;

import javax.annotation.concurrent.Immutable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.ripe.db.whois.common.domain.CIString.ciString;

@Immutable
public class MntRoutes {
    private static final Pattern MNT_ROUTES_PATTERN = Pattern.compile("(?i)^([A-Z](?:[A-Z0-9_-]){1,80})(?:[ ]+(\\{.*\\}|ANY))?$");
    private static final Splitter ADDRESS_PREFIX_RANGES_SPLITTER = Splitter.onPattern(",| ").trimResults().omitEmptyStrings();

    private final CIString maintainer;
    private final boolean anyRange;
    private final List<AddressPrefixRange> addressPrefixRanges;

    public MntRoutes(final String maintainer, final boolean anyRange, final List<AddressPrefixRange> addressPrefixRanges) {
        this.maintainer = ciString(maintainer);
        this.anyRange = anyRange;
        this.addressPrefixRanges = Collections.unmodifiableList(Lists.newArrayList(addressPrefixRanges));
    }

    public CIString getMaintainer() {
        return maintainer;
    }

    public boolean isAnyRange() {
        return anyRange || addressPrefixRanges.isEmpty();
    }

    public List<AddressPrefixRange> getAddressPrefixRanges() {
        return addressPrefixRanges;
    }

    @Override
    public int hashCode() { //TODO: untested
    	long hashSum = 0;
    	for(AddressPrefixRange a : addressPrefixRanges) {
    		hashSum += a.hashCode(); //compute hash sum manually, to simulate the behaviour of hashSet; ignore list order
    	}
    	return (toString() + hashSum).hashCode(); //combine as strings, then hash
    }
    
    @Override
    public String toString() { //TODO: untested
    	String ret = maintainer + " " + anyRange + " [";
    	
    	for(int i=0; i<3 && i<addressPrefixRanges.size(); i++) {
			if(i!=0)
				ret += ", ";
			ret += "(" + addressPrefixRanges.get(i).toString() + ")";
    	}
    	
    	if(addressPrefixRanges.size() > 3) //if more records than those listed, add dots
    		return ret + ", ...]";
    	else
    		return ret + "]";
    }
    
    @Override
    public boolean equals(final Object o) { //TODO: untested
    	if(o == this)
    		return true;
    	if(o == null || !(o instanceof MntRoutes))
    		return false;
    	else {
    		final MntRoutes that = (MntRoutes) o;
    		
    		//using HashSets here so as to disregard the order of the maintainer routes in the source lists.
    		final HashSet<AddressPrefixRange> thisAPR = new HashSet<AddressPrefixRange>();
    		final HashSet<AddressPrefixRange> thatAPR = new HashSet<AddressPrefixRange>();
    		
    		thisAPR.addAll(addressPrefixRanges);
    		thatAPR.addAll(that.addressPrefixRanges);
    		
    		return maintainer.equals(that.maintainer) && anyRange==that.anyRange && thisAPR.equals(thatAPR);
    	}
    }
    
    public static MntRoutes parse(final CIString value) {
        return parse(value.toString());
    }

    public static MntRoutes parse(final String value) {
        final Matcher matcher = MNT_ROUTES_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new AttributeParseException("Invalid syntax", value);
        }

        final String maintainer = matcher.group(1);

        final List<AddressPrefixRange> addressPrefixRanges = Lists.newArrayList();
        boolean anyRange = false;

        final String addressPrefixRangesString = matcher.group(2);
        if (addressPrefixRangesString != null) {
            if (addressPrefixRangesString.equalsIgnoreCase("ANY")) {
                anyRange = true;
            } else {
                final String noBrackets = addressPrefixRangesString.substring(1, addressPrefixRangesString.length() - 1);
                final Iterable<String> rangeStrings = ADDRESS_PREFIX_RANGES_SPLITTER.split(noBrackets);
                for (final String rangeString : rangeStrings) {
                    addressPrefixRanges.add(AddressPrefixRange.parse(rangeString));
                }
            }
        }

        return new MntRoutes(maintainer, anyRange, addressPrefixRanges);
    }
}
