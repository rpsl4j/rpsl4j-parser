package net.ripe.db.whois.common.rpsl.attrs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class IPAddressTest {

	private static final IPAddress addr1 = IPAddress.parse("1.1.1.1");
	private static final IPAddress addr2 = IPAddress.parse("1.1.1.1");
	private static final IPAddress addr3 = IPAddress.parse("1.1.1.2");
	
	//TODO: add ipv6 tests, and tests reaching beyond equality and hashcodes.
	
	@Test
	public void testEquality() {
		assertTrue("Self references should be equal", addr1.equals(addr1));
		assertTrue("Instances with the same address should be transitively equal under equals()", addr1.equals(addr2) && addr2.equals(addr1));
		assertFalse("Differing addresses should never return true from equals()", addr1.equals(addr3) || addr3.equals(addr1));
	
		//elementary
		assertFalse("equals(null) should return false", addr1.equals(null));
		assertFalse("equals(someOtherType) should return false", addr1.equals("Hello world"));
	}
	
	@Test
	public void testHashCode() {
		assertTrue("Equal valued instances should have the same hashCode", addr1.hashCode()==addr2.hashCode());
		assertTrue("Unequal valued instances should have differing hashCodes", addr1.hashCode()!=addr3.hashCode());
	}
	
	@Test
	public void testToString() {
		assertTrue("toString() should return address string with length", addr3.toString().equals("1.1.1.2/32"));
	}
}
