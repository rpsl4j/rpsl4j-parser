package net.ripe.db.whois.common.rpsl.attrs;

import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;

import org.junit.Test;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class DomainTest {

	
	//for equality and hashcode tests
	
	//ipv4
	private static final Domain domain1 = Domain.parse("200.193.193.in-addr.arpa");
	private static final Domain domain1_2 = Domain.parse("200.193.193.in-addr.arpa");
	private static final Domain domain2 = Domain.parse("200.193.190.in-addr.arpa"); //address differs
	
	//ipv6
	private static final Domain domain4 = Domain.parse("2.1.2.1.5.5.5.2.0.2.1.e164.arpa");
	private static final Domain domain4_2 = Domain.parse("2.1.2.1.5.5.5.2.0.2.1.e164.arpa");
	private static final Domain domain5 = Domain.parse("2.1.2.1.5.1.5.2.0.2.1.e164.arpa"); //TODO: Assuming that's valid for ipv6.. different address, same type hopefully
	private static final Domain domain6 = Domain.parse("0.0.0.0.8.f.7.0.1.0.0.2.IP6.ARPA");
	private static final Domain domain7 = Domain.parse("2.1.2.1.5.5.5.2.0.2.1.IP6.ARPA"); //probably going to fail parsing.. and didn't.. 
	
	
    @Test(expected = AttributeParseException.class)
    public void empty() {
        Domain.parse("");
    }

    @Test
    public void testEquals() {
    	assertTrue("equals(this) should be true", domain1.equals(domain1));
    	assertTrue("equals(object of same values) should be true", domain1.equals(domain1_2));
    	assertFalse("different domains shouldn't be equal", domain1.equals(domain2));
    	
    	assertFalse("Ipv4 and ipv6 domains should not be considered equal", domain1.equals(domain4) || domain4.equals(domain1)); //not that that's foolproof to test thoroughly..
    	
    	assertFalse("Different domain types shouldn't be considered equal", domain4.equals(domain7));
    	
    	assertTrue("(ipv6) Identical domains should be considered equal", domain4.equals(domain4_2));
    	
    	assertFalse("equals(null) should be false", domain1.equals(null));
    	assertFalse("equals(different type) should be false", domain1.equals("Hello world"));
    }
    
    @Test
    public void testHashCode() {
    	assertTrue("(ipv4)Identical domains should have matching hashcodes", domain1.hashCode()==domain1_2.hashCode());
    	assertFalse("(ipv4)Different domains hashcodes shouldn't match", domain1.hashCode()==domain2.hashCode());
    	
    	assertTrue("(ipv6) Identical domains should have matching hashcodes", domain4.hashCode()==domain4_2.hashCode());
    	assertFalse("(ipv6) Different domains shouldn't have matching hashcodes", domain4.hashCode()==domain5.hashCode() || domain5.hashCode()==domain4.hashCode());
    }
    
    @Test
    public void testToString() {
    	assertTrue("(ipv4) toString should return expected format", domain1.toString().equals("200.193.193.in-addr.arpa(193.193.200.0/24 INADDR not-dashed)"));
    	assertTrue("(ipv6) toString should return expected format", domain4.toString().equals("2.1.2.1.5.5.5.2.0.2.1.e164.arpa(E164 not-dashed)"));
    }
    
    @Test
    public void valid_ipv4() {
        final Domain domain = Domain.parse("200.193.193.in-addr.arpa");
        assertThat(domain.getValue(), is(ciString("200.193.193.in-addr.arpa")));
        assertThat((Ipv4Resource) domain.getReverseIp(), is(Ipv4Resource.parse("193.193.200/24")));
        assertThat(domain.getType(), is(Domain.Type.INADDR));
    }

    @Test
    public void ipv4_dash() {
        final Domain domain = Domain.parse("0-127.10.10.10.in-addr.arpa");
        assertThat(domain.getValue(), is(ciString("0-127.10.10.10.in-addr.arpa")));
        assertThat((Ipv4Resource) domain.getReverseIp(), is(Ipv4Resource.parse("10.10.10.0/25")));
        assertThat(domain.getType(), is(Domain.Type.INADDR));
    }

    @Test(expected = AttributeParseException.class)
    public void ipv4_dash_invalid_position() {
        Domain.parse("0-127.10.10.in-addr.arpa");
    }

    @Test(expected = AttributeParseException.class)
    public void ipv4_dash_range_0_255() {
        Domain.parse("0-255.10.10.in-addr.arpa");
    }

    @Test(expected = AttributeParseException.class)
    public void ipv4_dash_range_start_is_range_end() {
        Domain.parse("1-1.10.10.in-addr.arpa");
    }

    @Test
    public void ipv4_dash_non_prefix_range() {
        final Domain domain = Domain.parse("1-2.10.10.10.in-addr.arpa");
        assertThat(domain.getValue(), is(ciString("1-2.10.10.10.in-addr.arpa")));
        assertThat((Ipv4Resource) domain.getReverseIp(), is(Ipv4Resource.parse("10.10.10.1-10.10.10.2")));
        assertThat(domain.getType(), is(Domain.Type.INADDR));
    }

    @Test
    public void valid_ipv4_trailing_dot() {
        final Domain domain = Domain.parse("200.193.193.in-addr.arpa.");
        assertThat(domain.getValue(), is(ciString("200.193.193.in-addr.arpa")));
        assertThat((Ipv4Resource) domain.getReverseIp(), is(Ipv4Resource.parse("193.193.200/24")));
        assertThat(domain.getType(), is(Domain.Type.INADDR));
    }

    @Test
    public void valid_ipv6() {
        final Domain domain = Domain.parse("0.0.0.0.8.f.7.0.1.0.0.2.IP6.ARPA");
        assertThat(domain.getValue(), is(ciString("0.0.0.0.8.f.7.0.1.0.0.2.ip6.arpa")));
        assertThat((Ipv6Resource) domain.getReverseIp(), is(Ipv6Resource.parse("2001:7f8::/48")));
        assertThat(domain.getType(), is(Domain.Type.IP6));
    }

    @Test
    public void valid_ipv6_trailing_dot() {
        final Domain domain = Domain.parse("0.0.0.0.8.f.7.0.1.0.0.2.ip6.arpa.");
        assertThat(domain.getValue(), is(ciString("0.0.0.0.8.f.7.0.1.0.0.2.ip6.arpa")));
        assertThat((Ipv6Resource) domain.getReverseIp(), is(Ipv6Resource.parse("2001:7f8::/48")));
        assertThat(domain.getType(), is(Domain.Type.IP6));
    }

    @Test
    public void ipv4_prefix_32_allowed() {
        Domain domain = Domain.parse("200.193.193.193.in-addr.arpa.");
        assertThat(domain.getValue(), is(ciString("200.193.193.193.in-addr.arpa")));
    }

    @Test(expected = AttributeParseException.class)
    public void suffix() {
        Domain.parse("200.193.193.193.some-suffix.");
    }

    @Test(expected = AttributeParseException.class)
    public void suffix_almost_correct() {
        Domain.parse("200.193.193.in-addraarpa");
    }

    @Test
    public void end_with_domain_enum() {
        final Domain domain = Domain.parse("2.1.2.1.5.5.5.2.0.2.1.e164.arpa");
        assertThat(domain.endsWithDomain(ciString("a.ns.2.1.2.1.5.5.5.2.0.2.1.e164.arpa")), is(true));
    }

    @Test
    public void end_with_domain_enum_fails() {
        final Domain domain = Domain.parse("2.1.2.1.5.5.5.2.0.2.1.e164.arpa");
        assertThat(domain.endsWithDomain(ciString("a.ns.2.1.2.1.5.5.5.2.0.e164.arpa")), is(false));
    }

    @Test
    public void end_with_domain_ipv6() {
        final Domain domain = Domain.parse("0.0.0.0.8.f.7.0.1.0.0.2.IP6.ARPA");
        assertThat(domain.endsWithDomain(ciString("a.ns.0.0.0.0.8.f.7.0.1.0.0.2.IP6.ARPA")), is(true));
    }

    @Test
    public void end_with_domain_ipv6_fails() {
        final Domain domain = Domain.parse("0.0.0.0.8.f.7.0.1.0.0.2.IP6.ARPA");
        assertThat(domain.endsWithDomain(ciString("a.ns.0.0.0.8.f.7.0.1.0.0.2.IP6.ARPA")), is(false));
    }

    @Test
    public void end_with_domain_ipv4() {
        final Domain domain = Domain.parse("200.193.193.in-addr.arpa");
        assertThat(domain.endsWithDomain(ciString("200.193.193.in-addr.arpa")), is(true));
    }

    @Test
    public void end_with_domain_ipv4_fails() {
        final Domain domain = Domain.parse("200.193.193.in-addr.arpa");
        assertThat(domain.endsWithDomain(ciString("193.193.in-addr.arpa")), is(false));
    }

    @Test
    public void end_with_domain_ipv4_dash() {
        final Domain domain = Domain.parse("1-10.200.193.193.in-addr.arpa");
        assertThat(domain.endsWithDomain(ciString("n.s.5.200.193.193.in-addr.arpa")), is(true));
    }

    @Test
    public void end_with_domain_ipv4_dash_no_match() {
        final Domain domain = Domain.parse("1-10.200.193.193.in-addr.arpa");
        assertThat(domain.endsWithDomain(ciString("n.s.5.200a193.193.in-addr.arpa")), is(false));
    }

    @Test
    public void end_with_domain_ipv4_dash_outside_range_lower() {
        final Domain domain = Domain.parse("1-10.200.193.193.in-addr.arpa");
        assertThat(domain.endsWithDomain(ciString("n.s.0.200.193.193.in-addr.arpa")), is(false));
    }

    @Test
    public void end_with_domain_ipv4_dash_outside_range_upper() {
        final Domain domain = Domain.parse("1-10.200.193.193.in-addr.arpa");
        assertThat(domain.endsWithDomain(ciString("n.s.100.200.193.193.in-addr.arpa")), is(false));
    }

    @Test
    public void enum_domain() {
        final Domain domain = Domain.parse("2.1.2.1.5.5.5.2.0.2.1.e164.arpa");
        assertThat(domain.getValue(), is(ciString("2.1.2.1.5.5.5.2.0.2.1.e164.arpa")));
        assertNull(domain.getReverseIp());
        assertThat(domain.getType(), is(Domain.Type.E164));
    }
}
