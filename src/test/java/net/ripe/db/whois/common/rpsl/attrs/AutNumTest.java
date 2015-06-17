package net.ripe.db.whois.common.rpsl.attrs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AutNumTest {

	/**
	 * Baseline tests only; doesn't test all ripe code; focuses on new ABN code
	 */
	
	private static final AutNum autNum1_1 = AutNum.parse("AS29");
	private static final AutNum autNum1_2 = AutNum.parse("AS29");
	private static final AutNum autNum3 = AutNum.parse("AS1");
	
	@Test
	public void toStringTest() {
		assertTrue("toString() should format as expected", autNum1_1.toString().equals("AS29"));
	}
	
	@Test
	public void hashCodeTest() {
		assertTrue("Equivalent as objects should have matching hashcodes", autNum1_1.hashCode()==autNum1_2.hashCode());
		assertFalse("Objects representing different ASs should have differing hashcodes", autNum1_1.hashCode()==autNum3.hashCode());
	}
	
	@Test
	public void testEquality() {
		assertTrue("Equivalent autnums should be considered equal", autNum1_1.equals(autNum1_2) && autNum1_2.equals(autNum1_1));
		assertFalse("Differing autnums shouldn't be considered equal", autNum1_1.equals(autNum3) || autNum3.equals(autNum1_2));
	}
	
	//one example of a parse that should fail
	@Test(expected = AttributeParseException.class) 
	public void parseFailTest() {
		final AutNum autNum = AutNum.parse("S31");
	}
}
