package net.ripe.db.whois.common.rpsl.attrs;

import org.joda.time.LocalDate;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ChangedTest {

	private static final Changed changed1_1 = Changed.parse("someone@provider.com 20150601");
	private static final Changed changed1_2 = Changed.parse("someone@provider.com 20150601");
	private static final Changed changed2 = Changed.parse("someoneelse@provider.com 20150701");
	
	@Test
	public void testEquals() {
		assertTrue("Equal changes should be considered equal", changed1_1.equals(changed1_2) && changed1_2.equals(changed1_1));
		assertFalse("Differing changes shouldn't be considered equal", changed1_1.equals(changed2) || changed2.equals(changed1_2));
	}
	
	@Test
	public void testHashCode() {
		assertTrue("Equal change object should have matching hashcodes", changed1_1.hashCode()==changed1_2.hashCode());
		assertFalse("Differing change object should have unequal hashcodes", changed1_1.hashCode()==changed2.hashCode());
	}
	
	@Test
	public void testToString() {
		assertTrue("toString should format as expected", changed1_1.toString().equals("someone@provider.com 20150601"));
	}
	
    @Test(expected = AttributeParseException.class)
    public void empty() {
        Changed.parse("");
    }

    @Test(expected = AttributeParseException.class)
    public void no_email() {
        Changed.parse("20010101");
    }

    @Test(expected = AttributeParseException.class)
    public void invalid_date() {
        Changed.parse("a@a.a 13131313");
    }

    @Test(expected = AttributeParseException.class)
    public void too_long() {
        Changed.parse("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz 20010101");
    }

    @Test
    public void no_date() {
        final Changed subject = Changed.parse("foo@provider.com");

        assertThat(subject.getEmail(), is("foo@provider.com"));
        assertNull(subject.getDateString());
        assertNull(subject.getDate());
        assertThat(subject.toString(), is("foo@provider.com"));
    }

    @Test
    public void short_with_date() {
        final Changed subject = Changed.parse("a@a.a 20010101");

        assertThat(subject.getEmail(), is("a@a.a"));
        assertThat(subject.getDateString(), is("20010101"));
        assertThat(subject.getDate(), is(new LocalDate(2001, 1, 1)));
        assertThat(subject.toString(), is("a@a.a 20010101"));
    }

    @Test(expected = AttributeParseException.class)
    public void mixedUpDateAndEmail() {
        Changed subject = Changed.parse("20130112 b.was@infbud.pl");
    }

    @Test
    public void long_email_date() {
        final Changed subject = Changed.parse("'anthingcan1242go!@(&)#^!(&@#^21here\"@0.2345678901234567890123456789012345678901 20010101");

        assertThat(subject.getEmail(), is("'anthingcan1242go!@(&)#^!(&@#^21here\"@0.2345678901234567890123456789012345678901"));
        assertThat(subject.toString(), is("'anthingcan1242go!@(&)#^!(&@#^21here\"@0.2345678901234567890123456789012345678901 20010101"));
    }
}
