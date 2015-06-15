package net.ripe.db.whois.common.rpsl.attrs;

import org.junit.Test;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class MntRoutesTest {

	private static final MntRoutes subject1_1 = MntRoutes.parse("AS286-MNT {194.104.182.0/24^+}");
	private static final MntRoutes subject1_2 = MntRoutes.parse("AS286-MNT {194.104.182.0/24^+}");
	private static final MntRoutes subject2 = MntRoutes.parse("AS287-MNT {194.104.182.0/24^+}"); //different as
	private static final MntRoutes subject3 = MntRoutes.parse("AS286-MNT {194.104.192.0/24^+}"); //different ip
	private static final MntRoutes subject4 = MntRoutes.parse("TEST-MNT {194.9.240.0/24,194.9.241.0/24}");
	
	
	//for toString() tests
	private static final MntRoutes subject5 = MntRoutes.parse("RIPE-NCC-RPSL-MNT ANY");
	
	private static final MntRoutes subject6 = MntRoutes.parse("TEST-MNT {194.9.240.0/24,194.9.241.0/24,194.9.242.0/24,194.9.243.0/24,194.9.244.0/24,194.9.245.0/24,194.9.246.0/24}"); //long list ;)
	private static final MntRoutes subject7 = MntRoutes.parse("TEST-MNT {194.9.240.0/24,194.9.241.0/24,194.9.242.0/24,194.9.243.0/24}"); //4 element..
	private static final MntRoutes subject8 = MntRoutes.parse("TEST-MNT {194.9.240.0/24,194.9.241.0/24,194.9.242.0/24}"); //3 element..
	private static final MntRoutes subject9 = MntRoutes.parse("TEST-MNT {194.9.240.0/24,194.9.241.0/24}"); //2 element..
	
	@Test
	public void testToString() {
		assertTrue("toString formats as expcted", subject1_1.toString().equals("MntRoutes (maintainer:AS286-MNT, anyRange:no) {194.104.182.0/24^+}"));
		
		//test printing of anyRange false
		assertTrue("toString formats as expcted - anyRange:no", subject5.toString().equals("MntRoutes (maintainer:RIPE-NCC-RPSL-MNT, anyRange:yes)"));
		
		//test longer list
		assertTrue("toString formats as expcted - input: many entries", subject6.toString().equals("MntRoutes (maintainer:TEST-MNT, anyRange:no) {194.9.240.0/24, 194.9.241.0/24, 194.9.242.0/24, ...}"));
		
		//test border cases for prefix printing (max 3 prefixes will print, after which dots are printed
		//4 entries
		assertTrue("toString formats as expcted - input: 4 entries", subject7.toString().equals("MntRoutes (maintainer:TEST-MNT, anyRange:no) {194.9.240.0/24, 194.9.241.0/24, 194.9.242.0/24, ...}"));
		
		//3 entries
		assertTrue("toString formats as expcted - input: 3 entries", subject8.toString().equals("MntRoutes (maintainer:TEST-MNT, anyRange:no) {194.9.240.0/24, 194.9.241.0/24, 194.9.242.0/24}"));
		
		//2 entries
		assertTrue("toString formats as expcted - input: 2 entries", subject9.toString().equals("MntRoutes (maintainer:TEST-MNT, anyRange:no) {194.9.240.0/24, 194.9.241.0/24}"));
	}
	
	@Test
	public void testHashCode() {
		assertTrue("Equivalent maintainer objects shoud match on hashcode", subject1_1.hashCode()==subject1_2.hashCode());
		assertFalse("Differing maintainer objects shouldn't match on hashcode", subject1_1.hashCode()==subject2.hashCode() ||
																				subject1_1.hashCode()==subject3.hashCode() ||
																			 	subject2.hashCode()==subject3.hashCode());
	}
	
	@Test
	public void testEquals() {
		assertTrue("Equivalent maintainer object should be considered equal", subject1_1.equals(subject1_2) && subject1_2.equals(subject1_1));
		assertFalse("Differing maintainer object should be considered unequal", subject1_1.equals(subject2) || subject1_2.equals(subject3));
	}
	
	
    @Test(expected = AttributeParseException.class)
    public void empty() {
        MntRoutes.parse("");
    }

    @Test
    public void maintainer_only() {
        final MntRoutes subject = MntRoutes.parse("RIPE-NCC-RPSL-MNT");

        assertThat(subject.getMaintainer().toString(), is("RIPE-NCC-RPSL-MNT"));
        assertThat(subject.isAnyRange(), is(true));
        assertThat(subject.getAddressPrefixRanges(), hasSize(0));
    }

    @Test
    public void maintainer_with_any() {
        final MntRoutes subject = MntRoutes.parse("RIPE-NCC-RPSL-MNT ANY");

        assertThat(subject.getMaintainer().toString(), is("RIPE-NCC-RPSL-MNT"));
        assertThat(subject.isAnyRange(), is(true));
        assertThat(subject.getAddressPrefixRanges(), hasSize(0));
    }

    @Test(expected = AttributeParseException.class)
    public void maintainer_with_any_and_range() {
        MntRoutes.parse("RIPE-NCC-RPSL-MNT { ANY,194.104.182.0/24^+ }");
    }

    @Test
    public void maintainer_with_addres_prefix_range() {
        final MntRoutes subject = MntRoutes.parse("AS286-MNT {194.104.182.0/24^+}");

        assertThat(subject.getMaintainer().toString(), is("AS286-MNT"));
        assertThat(subject.isAnyRange(), is(false));
        assertThat(subject.getAddressPrefixRanges(), hasSize(1));
        assertThat(subject.getAddressPrefixRanges().get(0).toString(), is("194.104.182.0/24^+"));
    }

    @Test
    public void maintainer_with_addres_prefix_ranges() {
        final MntRoutes subject = MntRoutes.parse("TEST-MNT {194.9.240.0/24,194.9.241.0/24}");

        assertThat(subject.getMaintainer().toString(), is("TEST-MNT"));
        assertThat(subject.isAnyRange(), is(false));
        assertThat(subject.getAddressPrefixRanges(), hasSize(2));
        assertThat(subject.getAddressPrefixRanges().get(0).getIpInterval().toString(), is("194.9.240.0/24"));
        assertThat(subject.getAddressPrefixRanges().get(1).getIpInterval().toString(), is("194.9.241.0/24"));
    }

    @Test(expected = AttributeParseException.class)
    public void maintainer_with_any_inside_brackets() {
        MntRoutes.parse("TEST-MNT { ANY }");
    }

    @Test(expected = AttributeParseException.class)
    public void maintainer_with_any_inside_brackets_no_padding_space() {
        MntRoutes.parse("TEST-MNT {ANY}");
    }
}
