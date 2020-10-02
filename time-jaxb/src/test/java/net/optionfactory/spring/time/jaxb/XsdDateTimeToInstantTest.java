package net.optionfactory.spring.time.jaxb;

import java.io.StringReader;
import java.io.StringWriter;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.junit.Assert;
import org.junit.Test;

public class XsdDateTimeToInstantTest {

    private final XsdDateTimeToInstant adapter = new XsdDateTimeToInstant();

    @Test
    public void canParseDateWithOffset() {
        final Instant got = adapter.unmarshal("2003-02-01T04:05:06+01:00");
        Assert.assertEquals(OffsetDateTime.of(2003, 2, 1, 4, 5, 6, 0, ZoneOffset.ofHours(1)).toInstant(), got);
    }

    @Test(expected = DateTimeParseException.class)
    public void cannotParseDateWithOffset() {
        adapter.unmarshal("2003-02-01T04:05:06");
    }

    @XmlRootElement(name = "B")
    public static class BeanWithInstant {

        @XmlJavaTypeAdapter(XsdDateTimeToInstant.class)
        public Instant at;
    }

    @Test
    public void canMarshalNotNull() throws JAXBException {
        final BeanWithInstant b = new BeanWithInstant();
        b.at = Instant.EPOCH;
        final String got = Marshalling.marshall(b);
        final String expected = "<at>1970-01-01T00:00:00Z</at>";
        Assert.assertTrue(String.format("expected to contain: %s, got: %s", expected, got), got.contains(expected));
    }

    @Test
    public void canUnmarshalNotNull() throws JAXBException {
        BeanWithInstant b = Marshalling.unmarshall("<B><at>1970-01-01T00:00:00Z</at></B>", BeanWithInstant.class);
        Assert.assertEquals(Instant.EPOCH, b.at);
    }
    
    @Test
    public void canMarshalNull() throws JAXBException {
        final BeanWithInstant b = new BeanWithInstant();
        b.at = null;
        final String got = Marshalling.marshall(b);
        final String expected = "<B/>";
        Assert.assertTrue(String.format("expected to contain: %s, got: %s", expected, got), got.contains(expected));
    }

    @Test
    public void canUnmarshalNull() throws JAXBException {
        BeanWithInstant b1 = Marshalling.unmarshall("<B/>", BeanWithInstant.class);
        Assert.assertEquals(null, b1.at);
        BeanWithInstant b2 = Marshalling.unmarshall("<B><at xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/></B>", BeanWithInstant.class);
        Assert.assertEquals(null, b2.at);
        
    }

}
