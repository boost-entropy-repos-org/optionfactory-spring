package net.optionfactory.spring.time.jaxb;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.junit.Assert;
import org.junit.Test;

public class XsdDateTimeToOffsetDateTimeTest {

    private final XsdDateTimeToOffsetDateTime adapter = new XsdDateTimeToOffsetDateTime();

    @Test
    public void canParseDateWithOffset() {
        final OffsetDateTime got = adapter.unmarshal("2003-02-01T04:05:06+01:00");
        Assert.assertEquals(OffsetDateTime.of(2003, 2, 1, 4, 5, 6, 0, ZoneOffset.ofHours(1)), got);
    }

    @Test(expected = DateTimeParseException.class)
    public void cannotParseDateWithoutOffset() {
        adapter.unmarshal("2003-02-01T04:05:06");
    }

    
    @XmlRootElement(name = "B")
    public static class BeanWithOffsetDateTime {

        @XmlJavaTypeAdapter(XsdDateTimeToOffsetDateTime.class)
        public OffsetDateTime at;
    }

    @Test
    public void canMarshalNotNull() throws JAXBException {
        final BeanWithOffsetDateTime b = new BeanWithOffsetDateTime();
        b.at = OffsetDateTime.of(2020, 2, 1, 20, 19, 18, 0, ZoneOffset.UTC);
        final String got = Marshalling.marshall(b);
        final String expected = "<at>2020-02-01T20:19:18Z</at>";
        Assert.assertTrue(String.format("expected to contain: %s, got: %s", expected, got), got.contains(expected));
    }

    @Test
    public void canUnmarshalNotNull() throws JAXBException {
        BeanWithOffsetDateTime b = Marshalling.unmarshall("<B><at>2020-02-01T20:19:18Z</at></B>", BeanWithOffsetDateTime.class);
        Assert.assertEquals(OffsetDateTime.of(2020, 2, 1, 20, 19, 18, 0, ZoneOffset.UTC), b.at);
    }    
    
    @Test
    public void canMarshalNull() throws JAXBException {
        final BeanWithOffsetDateTime b = new BeanWithOffsetDateTime();
        final String got = Marshalling.marshall(b);
        final String expected = "<B/>";
        Assert.assertTrue(String.format("expected to contain: %s, got: %s", expected, got), got.contains(expected));
    }

    @Test
    public void canUnmarshalNull() throws JAXBException {
        BeanWithOffsetDateTime b1 = Marshalling.unmarshall("<B/>", BeanWithOffsetDateTime.class);
        Assert.assertEquals(null, b1.at);
        BeanWithOffsetDateTime b2 = Marshalling.unmarshall("<B><at/></B>", BeanWithOffsetDateTime.class);
        Assert.assertEquals(null, b2.at);
        BeanWithOffsetDateTime b3 = Marshalling.unmarshall("<B><at xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/></B>", BeanWithOffsetDateTime.class);
        Assert.assertEquals(null, b3.at);
    }        
        
}
