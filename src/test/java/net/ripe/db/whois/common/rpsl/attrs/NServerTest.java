package net.ripe.db.whois.common.rpsl.attrs;

import net.ripe.db.whois.common.ip.Ipv4Resource;
import net.ripe.db.whois.common.ip.Ipv6Resource;

import org.junit.Test;

import static net.ripe.db.whois.common.domain.CIString.ciString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class NServerTest {
    
	private static final NServer nServer1_1 = NServer.parse("dns.comcor.ru 194.0.0.0");
	private static final NServer nServer1_2 = NServer.parse("dns.comcor.ru 194.0.0.0");
	private static final NServer nServer2 = NServer.parse("dns.somedomain.com 200.0.0.0");
	
	@Test
	public void testEquals() {
		assertTrue("Equivalent NServer objects should be equal", nServer1_1.equals(nServer1_2) && nServer1_2.equals(nServer1_1));
		assertFalse("Differing NServer objects should be unequal", nServer1_1.equals(nServer2) || nServer2.equals(nServer1_2));
	}
	
	@Test
	public void testHashCode() {
		assertTrue("Equivalent NServers should have matching hashcodes", nServer1_1.hashCode()==nServer1_2.hashCode());
		assertFalse("Differing NServers should have different hashcodes", nServer1_1.hashCode()==nServer2.hashCode());
	}
	
	
	@Test(expected = AttributeParseException.class)
    public void empty() {
        NServer.parse("");
    }

    @Test(expected = AttributeParseException.class)
    public void hostname_invalid() {
        NServer.parse("$");
    }

    @Test
    public void hostname_only() {
        final NServer nServer = NServer.parse("dns.comcor.ru");
        assertThat(nServer.getHostname(), is(ciString("dns.comcor.ru")));
        assertNull(nServer.getIpInterval());
        assertThat(nServer.toString(), is("dns.comcor.ru"));
    }

    @Test
    public void hostname_trailing_dot() {
        final NServer nServer = NServer.parse("dns.comcor.ru.");
        assertThat(nServer.getHostname(), is(ciString("dns.comcor.ru")));
        assertNull(nServer.getIpInterval());
        assertThat(nServer.toString(), is("dns.comcor.ru"));
    }

    @Test
    public void hostname_and_ipv4() {
        final NServer nServer = NServer.parse("dns.comcor.ru 194.0.0.0");
        assertThat(nServer.getHostname(), is(ciString("dns.comcor.ru")));
        assertThat((Ipv4Resource) nServer.getIpInterval(), is(Ipv4Resource.parse("194.0.0.0")));
        assertThat(nServer.toString(), is("dns.comcor.ru 194.0.0.0"));
    }

    @Test(expected = AttributeParseException.class)
    public void hostname_and_ipv4_range_24() {
        NServer.parse("dns.comcor.ru 194.0.0.0/24");
    }

    @Test
    public void hostname_and_ipv4_range_32() {
        final NServer nServer = NServer.parse("dns.comcor.ru 194.0.0.0/32");
        assertThat(nServer.getHostname(), is(ciString("dns.comcor.ru")));
        assertThat((Ipv4Resource) nServer.getIpInterval(), is(Ipv4Resource.parse("194.0.0.0")));
        assertThat(nServer.toString(), is("dns.comcor.ru 194.0.0.0"));
    }

    @Test
    public void hostname_trailing_dot_and_ipv4() {
        final NServer nServer = NServer.parse("dns.comcor.ru. 194.0.0.0");
        assertThat(nServer.getHostname(), is(ciString("dns.comcor.ru")));
        assertThat((Ipv4Resource) nServer.getIpInterval(), is(Ipv4Resource.parse("194.0.0.0")));
        assertThat(nServer.toString(), is("dns.comcor.ru 194.0.0.0"));
    }

    @Test(expected = AttributeParseException.class)
    public void hostname_and_ipv4_list() {
        NServer.parse("dns.comcor.ru 194.0.0.0 194.0.0.0");
    }

    @Test
    public void ipv4_only() {
        final NServer nServer = NServer.parse("194.0.0.0");
        assertThat(nServer.getHostname(), is(ciString("194.0.0.0")));
        assertNull(nServer.getIpInterval());
        assertThat(nServer.toString(), is("194.0.0.0"));
    }

    @Test
    public void hostname_and_ipv6() {
        final NServer nServer = NServer.parse("dns.comcor.ru f::1");
        assertThat(nServer.getHostname(), is(ciString("dns.comcor.ru")));
        assertThat((Ipv6Resource) nServer.getIpInterval(), is(Ipv6Resource.parse("f::1")));
        assertThat(nServer.toString(), is("dns.comcor.ru f::1"));
    }

    @Test(expected = AttributeParseException.class)
    public void hostname_and_invalid_ip() {
        NServer.parse("dns.comcor.ru dns.comcor.ru");
    }
}
