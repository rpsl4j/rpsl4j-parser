package net.ripe.db.whois.common.rpsl.attrs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class RangeOperationTest {

	/**LOW COVERAGE TEST! - Only equals() toString() and hashCode() are tested here.
	//(Test created to test code written to support attribute equality) */
	
	//anything more specific, that is of length 16. ie for 1.2.3.4/8, 1.100.2.3/16
	private final static RangeOperation rangeOperation1_1 = RangeOperation.parse("^16", 8, 32); //simulate x.x.x.x/8 (ipv4, so maxrange of 32, and a mask length of 8)
	private final static RangeOperation rangeOperation1_2 = RangeOperation.parse("^16", 8, 32); //duplicate of above
	
	private final static RangeOperation rangeOperation2 = RangeOperation.parse("^16", 8, 128); //on slightly shaky ground here.. ipv6..
	
	private final static RangeOperation rangeOperation3 = RangeOperation.parse("^24-32", 8, 32); //something to test that m is actually separate..
	
	private final static RangeOperation rangeOperation4 = RangeOperation.parse("^-", 8, 32); //should return 9-32.. ok, 32-9. Whatever :P Who specifies ranges backwards :/
	
	private final static RangeOperation rangeOperation5_1 = RangeOperation.parse("^24-24", 8, 32); //pointless, but legal I spose, and equivalent to the following..
	private final static RangeOperation rangeOperation5_2 = RangeOperation.parse("^24", 8, 32);
	
	private final static RangeOperation rangeOperation6 = RangeOperation.parse("^+", 8, 32); //expect 8-32

	@Test (expected = AttributeParseException.class) //borrowing that idea from MntRoutesTest.. 
	public void testSomeSanity() {
		RangeOperation illegalPrefix = RangeOperation.parse("^16", 24, 32); //break stuff..?
		fail("Prefix shorter than netmask should throw an exception");
	}
	
	
	@Test
	public void testEquality() {
		assertTrue("Equivalent object should be considered equal", rangeOperation1_1.equals(rangeOperation1_2) && rangeOperation1_2.equals(rangeOperation1_1));
		
		assertFalse("Differing objects should be considered unequal",	rangeOperation1_1.equals(rangeOperation3) ||
																		//rangeOperation1_1.equals(rangeOperation2) || //not different because address length isn't maintained anywhere
																		rangeOperation1_1.equals(rangeOperation4));
	}
	
	@Test
	public void testHashCode() {
		assertTrue("Equivalent object's hashcodes should match", rangeOperation1_1.hashCode()==rangeOperation1_2.hashCode() &&
																 rangeOperation5_1.hashCode()==rangeOperation5_2.hashCode());
		
		assertFalse("Differing range operation object should have differing hashcodes",	rangeOperation1_1.hashCode()==rangeOperation4.hashCode() ||
																						rangeOperation1_1.hashCode()==rangeOperation3.hashCode());
		//rangeOperation1_1.hashCode()==rangeOperation2.hashCode()); //demonstrates an interesting case this; the max length for the address isn't maintained..
	}
	
	@Test
	public void testToString() {
//		System.out.println(rangeOperation1_1);
		assertTrue("toString() should format as expected", rangeOperation1_1.toString().equals("RangeOperation [n:16 m:16]") &&
														   rangeOperation2.toString().equals("RangeOperation [n:16 m:16]") &&
														   rangeOperation3.toString().equals("RangeOperation [n:24 m:32]") &&
														   rangeOperation4.toString().equals("RangeOperation [n:9 m:32]") &&
														   rangeOperation5_1.toString().equals("RangeOperation [n:24 m:24]") &&
														   rangeOperation6.toString().equals("RangeOperation [n:8 m:32]")
														   );
	}
	
}
