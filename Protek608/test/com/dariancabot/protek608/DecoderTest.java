package com.dariancabot.protek608;

import com.dariancabot.protek608.exceptions.ProtocolException;
import static org.hamcrest.Matchers.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;


/**
 *
 * @author Darian Cabot
 */
public class DecoderTest
{
    private int lastEventData = 12;


    public DecoderTest()
    {
    }


    @BeforeClass
    public static void setUpClass()
    {
    }


    @AfterClass
    public static void tearDownClass()
    {
    }


    @Before
    public void setUp()
    {
    }


    @After
    public void tearDown()
    {
    }


    //-----------------------------------------------------------------------
    /**
     * Test of the EventListener method, of class Decoder.
     *
     * This should trigger upon successful packet decode.
     */
    public class EventListenerImpl implements EventListener
    {
        @Override
        public void dataUpdateEvent()
        {
            lastEventData = 34;
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Rule for testing of correctly thrown Exceptions.
     */
    @Rule
    public ExpectedException thrown = ExpectedException.none();


    //-----------------------------------------------------------------------
    /**
     * Test of decodeSerialData method, of class Decoder.
     *
     * Invalid packet lengths, should throw ProtocolException.
     */
    @Test
    public void testDecodeSerialDataExceptionPacketLength()
    {
        byte[] buffer;
        Data data = new Data();
        Decoder decoder = new Decoder(data);

        buffer = new byte[42]; // Packet length too short.
        thrown.expect(ProtocolException.class);
        thrown.expectMessage("Decode error: Packet length is 42, but should be 43.");
        decoder.decodeSerialData(buffer);

        buffer = new byte[44]; // Packet length too long.
        thrown.expect(ProtocolException.class);
        thrown.expectMessage("Decode error: Packet length is 44, but should be 43.");
        decoder.decodeSerialData(buffer);
    }


    //-----------------------------------------------------------------------
    /**
     * Test of decodeSerialData method, of class Decoder.
     *
     * Blank packet (zero) packet, should throw ProtocolException.
     */
    @Test
    public void testDecodeSerialData1()
    {
        Data data = new Data();
        Decoder decoder = new Decoder(data);

        byte[] buffer = new byte[43];

        assertThat(data.mainValue.toString(), equalTo(null));

        thrown.expect(ProtocolException.class);
        thrown.expectMessage("Decode error: Packet start byte 0x5b not found at start of packet.");
        decoder.decodeSerialData(buffer);

        assertThat(data.mainValue.toString(), equalTo(null));
    }


    //-----------------------------------------------------------------------
    /**
     * Test of decodeSerialData method, of class Decoder.
     *
     * Invalid packet due to wrong start byte, should throw ProtocolException.
     */
    @Test
    public void testDecodeSerialData2()
    {
        Data data = new Data();
        Decoder decoder = new Decoder(data);

        // Packet with invalid start character.
        byte[] buffer =
        {
            0x11, 0x0d, 0x0f, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x02, 0x08, 0x0f, 0x0d, 0x0f,
            0x05, 0x0f, 0x05, 0x00, 0x0c, 0x00, 0x00, 0x00, 0x00, 0x05, 0x0b, 0x06, 0x00, 0x00, 0x02,
            0x00, 0x00, 0x00, 0x00, 0x08, 0x04, 0x00, 0x05, 0x0f, 0x06, 0x0b, 0x00, 0x5d
        };

        thrown.expect(ProtocolException.class);
        thrown.expectMessage("Decode error: Packet start byte 0x5b not found at start of packet.");
        decoder.decodeSerialData(buffer);
    }


    //-----------------------------------------------------------------------
    /**
     * Test of decodeSerialData method, of class Decoder.
     *
     * Invalid packet due to wrong end byte, should throw ProtocolException.
     */
    @Test
    public void testDecodeSerialData3()
    {
        Data data = new Data();
        Decoder decoder = new Decoder(data);

        // Packet with invalid end character.
        byte[] buffer =
        {
            0x5b, 0x0d, 0x0f, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x02, 0x08, 0x0f, 0x0d, 0x0f,
            0x05, 0x0f, 0x05, 0x00, 0x0c, 0x00, 0x00, 0x00, 0x00, 0x05, 0x0b, 0x06, 0x00, 0x00, 0x02,
            0x00, 0x00, 0x00, 0x00, 0x08, 0x04, 0x00, 0x05, 0x0f, 0x06, 0x0b, 0x00, 0x11
        };

        thrown.expect(ProtocolException.class);
        thrown.expectMessage("Decode error: Packet end byte 0x5d not found at end of packet.");
        decoder.decodeSerialData(buffer);
    }


    //-----------------------------------------------------------------------
    /**
     * Test of decodeSerialData method, of class Decoder.
     *
     * Valid packet, testing for decode accuracy.
     */
    @Test
    public void testDecodeSerialData4()
    {
        Data data = new Data();
        Decoder decoder = new Decoder(data);

        byte[] buffer =
        {
            0x5b, 0x0d, 0x0f, 0x05, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x02, 0x08, 0x0f, 0x0d, 0x0f,
            0x05, 0x0f, 0x05, 0x00, 0x0c, 0x00, 0x00, 0x00, 0x00, 0x05, 0x0b, 0x06, 0x00, 0x00, 0x02,
            0x00, 0x00, 0x00, 0x00, 0x08, 0x04, 0x00, 0x05, 0x0f, 0x06, 0x0b, 0x00, 0x5d
        };

        EventListener instance = new EventListenerImpl();
        decoder.setEventListener(instance);

        assertThat("Initialisation of test variable failed", lastEventData, equalTo(12));

        decoder.decodeSerialData(buffer);

        assertThat("Event failed to update test variable", lastEventData, equalTo(34));

        assertThat(data.mainValue.getValue(), equalTo("0.0015"));
        assertThat(data.mainValue.getValueVerbatim(), equalTo(" 0.0015"));
        assertThat(data.mainValue.getValueDouble(), equalTo(0.0015));

        assertThat(data.subValue.getValue(), equalTo("10.50"));
        assertThat(data.subValue.getValueVerbatim(), equalTo("  10.50"));
        assertThat(data.subValue.getValueDouble(), equalTo(10.50));

        assertThat(data.barGraph, equalTo(4));

        assertThat("audio flag", data.flags.audio, equalTo(false));
        assertThat("autoOff flag", data.flags.autoOff, equalTo(true));
        assertThat("avg flag", data.flags.avg, equalTo(false));
        assertThat("diode flag", data.flags.diode, equalTo(false));
        assertThat("duty flag", data.flags.duty, equalTo(false));
        assertThat("goNg flag", data.flags.goNg, equalTo(false));
        assertThat("hold flag", data.flags.hold, equalTo(false));
        assertThat("max flag", data.flags.max, equalTo(false));
        assertThat("min flag", data.flags.min, equalTo(false));
        assertThat("neg flag", data.flags.neg, equalTo(false));
        assertThat("negPeak flag", data.flags.negPeak, equalTo(false));
        assertThat("negPercent flag", data.flags.negPercent, equalTo(false));
        assertThat("pos flag", data.flags.pos, equalTo(false));
        assertThat("posPeak flag", data.flags.posPeak, equalTo(false));
        assertThat("posPercent flag", data.flags.posPercent, equalTo(false));
        assertThat("pulse flag", data.flags.pulse, equalTo(false));
        assertThat("range flag", data.flags.range, equalTo(false));
        assertThat("recall flag", data.flags.recall, equalTo(false));
        assertThat("ref flag", data.flags.ref, equalTo(false));
        assertThat("rel flag", data.flags.rel, equalTo(false));
        assertThat("rs232 flag", data.flags.rs232, equalTo(true));
        assertThat("store flag", data.flags.store, equalTo(false));
    }


    //-----------------------------------------------------------------------
    /**
     * Test of decodeSerialData method, of class Decoder.
     *
     * Valid packet, testing for decode accuracy.
     */
    @Test
    public void testDecodeSerialData5()
    {
        Data data = new Data();
        Decoder decoder = new Decoder(data);

        byte[] buffer =
        {
            0x5b, 0x05, 0x0f, 0x0d, 0x0f, 0x05, 0x0f, 0x02, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0b,
            0x06, 0x06, 0x06, 0x00, 0x08, 0x00, 0x00, 0x00, 0x04, 0x02, 0x0e, 0x02, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x0c, 0x00, 0x05, 0x00, 0x05, 0x0f, 0x02, 0x5d
        };

        EventListener instance = new EventListenerImpl();
        decoder.setEventListener(instance);

        assertThat("Initialisation of test variable failed", lastEventData, equalTo(12));

        decoder.decodeSerialData(buffer);

        assertThat("Event failed to update test variable", lastEventData, equalTo(34));

        assertThat(data.mainValue.getValue(), equalTo("5hrt"));
        assertThat(data.mainValue.getValueVerbatim(), equalTo("  5hrt"));
        assertThat(data.mainValue.getValueDouble(), equalTo(null));

        assertThat(data.subValue.getValue(), equalTo("00.001"));
        assertThat(data.subValue.getValueVerbatim(), equalTo(" 00.001"));
        assertThat(data.subValue.getValueDouble(), equalTo(0.001));

        assertThat(data.barGraph, equalTo(null)); // Bar graph not shown.

        assertThat("audio flag", data.flags.audio, equalTo(true));
        assertThat("autoOff flag", data.flags.autoOff, equalTo(false));
        assertThat("avg flag", data.flags.avg, equalTo(false));
        assertThat("diode flag", data.flags.diode, equalTo(false));
        assertThat("duty flag", data.flags.duty, equalTo(false));
        assertThat("goNg flag", data.flags.goNg, equalTo(false));
        assertThat("hold flag", data.flags.hold, equalTo(false));
        assertThat("max flag", data.flags.max, equalTo(false));
        assertThat("min flag", data.flags.min, equalTo(false));
        assertThat("neg flag", data.flags.neg, equalTo(false));
        assertThat("negPeak flag", data.flags.negPeak, equalTo(false));
        assertThat("negPercent flag", data.flags.negPercent, equalTo(false));
        assertThat("pos flag", data.flags.pos, equalTo(false));
        assertThat("posPeak flag", data.flags.posPeak, equalTo(false));
        assertThat("posPercent flag", data.flags.posPercent, equalTo(false));
        assertThat("pulse flag", data.flags.pulse, equalTo(false));
        assertThat("range flag", data.flags.range, equalTo(true));
        assertThat("recall flag", data.flags.recall, equalTo(false));
        assertThat("ref flag", data.flags.ref, equalTo(false));
        assertThat("rel flag", data.flags.rel, equalTo(false));
        assertThat("rs232 flag", data.flags.rs232, equalTo(true));
        assertThat("store flag", data.flags.store, equalTo(false));
    }


    //-----------------------------------------------------------------------
    /**
     * Test of decodeSerialData method, of class Decoder.
     *
     * Valid packet, testing for decode accuracy.
     */
    @Test
    public void testDecodeSerialData6()
    {
        Data data = new Data();
        Decoder decoder = new Decoder(data);

        byte[] buffer =
        {
            0x5b, 0x07, 0x0c, 0x07, 0x0c, 0x07, 0x07, 0x01, 0x00, 0x03, 0x07, 0x00, 0x0d, 0x03, 0x0d,
            0x0b, 0x03, 0x05, 0x00, 0x0c, 0x08, 0x00, 0x00, 0x0f, 0x05, 0x0b, 0x06, 0x00, 0x00, 0x02,
            0x00, 0x04, 0x0c, 0x0a, 0x02, 0x00, 0x02, 0x07, 0x0f, 0x0a, 0x04, 0x05, 0x5d
        };

        EventListener instance = new EventListenerImpl();
        decoder.setEventListener(instance);

        assertThat("Initialisation of test variable failed", lastEventData, equalTo(12));

        decoder.decodeSerialData(buffer);

        assertThat("Event failed to update test variable", lastEventData, equalTo(34));

        assertThat(data.mainValue.getValue(), equalTo("22.705"));
        assertThat(data.mainValue.getValueVerbatim(), equalTo(" 22.705")); // 22.6 V
        assertThat(data.mainValue.getValueDouble(), equalTo(22.705));

        assertThat(data.subValue.getValue(), equalTo("Addr.8"));
        assertThat(data.subValue.getValueVerbatim(), equalTo(" Addr.8")); // mA
        assertThat(data.subValue.getValueDouble(), equalTo(null));

        assertThat(data.barGraph, equalTo(22703));

        assertThat("audio flag", data.flags.audio, equalTo(false));
        assertThat("autoOff flag", data.flags.autoOff, equalTo(true));
        assertThat("avg flag", data.flags.avg, equalTo(false));
        assertThat("diode flag", data.flags.diode, equalTo(true));
        assertThat("duty flag", data.flags.duty, equalTo(false));
        assertThat("goNg flag", data.flags.goNg, equalTo(false));
        assertThat("hold flag", data.flags.hold, equalTo(false));
        assertThat("max flag", data.flags.max, equalTo(false));
        assertThat("min flag", data.flags.min, equalTo(false));
        assertThat("neg flag", data.flags.neg, equalTo(false));
        assertThat("negPeak flag", data.flags.negPeak, equalTo(false));
        assertThat("negPercent flag", data.flags.negPercent, equalTo(false));
        assertThat("pos flag", data.flags.pos, equalTo(false));
        assertThat("posPeak flag", data.flags.posPeak, equalTo(false));
        assertThat("posPercent flag", data.flags.posPercent, equalTo(false));
        assertThat("pulse flag", data.flags.pulse, equalTo(false));
        assertThat("range flag", data.flags.range, equalTo(false));
        assertThat("recall flag", data.flags.recall, equalTo(false));
        assertThat("ref flag", data.flags.ref, equalTo(false));
        assertThat("rel flag", data.flags.rel, equalTo(false));
        assertThat("rs232 flag", data.flags.rs232, equalTo(true));
        assertThat("store flag", data.flags.store, equalTo(true));
    }


    //-----------------------------------------------------------------------
    /**
     * Test of decodeSerialData method, of class Decoder.
     *
     * Valid packet, testing for decode accuracy.
     */
    @Test
    public void testDecodeSerialData7()
    {
        Data data = new Data();
        Decoder decoder = new Decoder(data);

        byte[] buffer =
        {
            0x5b, 0x0d, 0x0f, 0x05, 0x00, 0x05, 0x0f, 0x00, 0x02, 0x01, 0x00, 0x00, 0x00, 0x00, 0x0f,
            0x05, 0x0f, 0x0d, 0x00, 0x0c, 0x00, 0x00, 0x00, 0x0f, 0x05, 0x0f, 0x05, 0x02, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x00, 0x03, 0x0d, 0x05, 0x03, 0x04, 0x5d
        };

        EventListener instance = new EventListenerImpl();
        decoder.setEventListener(instance);

        assertThat("Initialisation of test variable failed", lastEventData, equalTo(12));

        decoder.decodeSerialData(buffer);

        assertThat("Event failed to update test variable", lastEventData, equalTo(34));

        assertThat(data.mainValue.getValue(), equalTo("00.00"));
        assertThat(data.mainValue.getValueVerbatim(), equalTo("  00.00")); // 22.6 V
        assertThat(data.mainValue.getValueDouble(), equalTo(0.0));

        assertThat(data.subValue.getValue(), equalTo("010.72"));
        assertThat(data.subValue.getValueVerbatim(), equalTo(" 010.72")); // mA
        assertThat(data.subValue.getValueDouble(), equalTo(010.72));

        assertThat(data.barGraph, equalTo(0)); // Minimum.

        assertThat("audio flag", data.flags.audio, equalTo(false));
        assertThat("autoOff flag", data.flags.autoOff, equalTo(true));
        assertThat("avg flag", data.flags.avg, equalTo(false));
        assertThat("diode flag", data.flags.diode, equalTo(false));
        assertThat("duty flag", data.flags.duty, equalTo(false));
        assertThat("goNg flag", data.flags.goNg, equalTo(false));
        assertThat("hold flag", data.flags.hold, equalTo(false));
        assertThat("max flag", data.flags.max, equalTo(false));
        assertThat("min flag", data.flags.min, equalTo(false));
        assertThat("neg flag", data.flags.neg, equalTo(false));
        assertThat("negPeak flag", data.flags.negPeak, equalTo(false));
        assertThat("negPercent flag", data.flags.negPercent, equalTo(false));
        assertThat("pos flag", data.flags.pos, equalTo(false));
        assertThat("posPeak flag", data.flags.posPeak, equalTo(false));
        assertThat("posPercent flag", data.flags.posPercent, equalTo(false));
        assertThat("pulse flag", data.flags.pulse, equalTo(false));
        assertThat("range flag", data.flags.range, equalTo(false));
        assertThat("recall flag", data.flags.recall, equalTo(false));
        assertThat("ref flag", data.flags.ref, equalTo(false));
        assertThat("rel flag", data.flags.rel, equalTo(false));
        assertThat("rs232 flag", data.flags.rs232, equalTo(true));
        assertThat("store flag", data.flags.store, equalTo(false));
    }


    //-----------------------------------------------------------------------
    /**
     * Test of decodeSerialData method, of class Decoder.
     *
     * Valid packet, testing for decode accuracy.
     */
    @Test
    public void testDecodeSerialData8()
    {
        Data data = new Data();
        Decoder decoder = new Decoder(data);

        // Auto Off, RS232, .OL Mohm, full bar graph, 2.5 V
        byte[] buffer =
        {
            0x5B, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x03, 0x0F, 0x00, 0x00, 0x00, 0x00,
            0x08, 0x0F, 0x05, 0x00, 0x0C, 0x00, 0x00, 0x00, 0x0E, 0x00, 0x00, 0x00, 0x00, 0x02, 0x00,
            0x01, 0x0F, 0x0F, 0x0F, 0x00, 0x02, 0x00, 0x06, 0x0B, 0x0B, 0x0D, 0x02, 0x5D
        };

        EventListener instance = new EventListenerImpl();
        decoder.setEventListener(instance);

        assertThat("Initialisation of test variable failed", lastEventData, equalTo(12));

        decoder.decodeSerialData(buffer);

        assertThat("Event failed to update test variable", lastEventData, equalTo(34));

        assertThat(data.mainValue.getValue(), equalTo(".0L"));
        assertThat(data.mainValue.getValueVerbatim(), equalTo("   .0L ")); // 22.6 V
        assertThat(data.mainValue.getValueDouble(), equalTo(null));

        assertThat(data.subValue.getValue(), equalTo("2.5"));
        assertThat(data.subValue.getValueVerbatim(), equalTo("    2.5")); // mA
        assertThat(data.subValue.getValueDouble(), equalTo(2.5));

        //System.out.println("Bar graph: " + Integer.toBinaryString(data.barGraph));
        assertThat(data.barGraph, equalTo(32767)); // Maximum.

        assertThat("audio flag", data.flags.audio, equalTo(false));
        assertThat("autoOff flag", data.flags.autoOff, equalTo(true));
        assertThat("avg flag", data.flags.avg, equalTo(false));
        assertThat("diode flag", data.flags.diode, equalTo(false));
        assertThat("duty flag", data.flags.duty, equalTo(false));
        assertThat("goNg flag", data.flags.goNg, equalTo(false));
        assertThat("hold flag", data.flags.hold, equalTo(false));
        assertThat("max flag", data.flags.max, equalTo(false));
        assertThat("min flag", data.flags.min, equalTo(false));
        assertThat("neg flag", data.flags.neg, equalTo(false));
        assertThat("negPeak flag", data.flags.negPeak, equalTo(false));
        assertThat("negPercent flag", data.flags.negPercent, equalTo(false));
        assertThat("pos flag", data.flags.pos, equalTo(false));
        assertThat("posPeak flag", data.flags.posPeak, equalTo(false));
        assertThat("posPercent flag", data.flags.posPercent, equalTo(false));
        assertThat("pulse flag", data.flags.pulse, equalTo(false));
        assertThat("range flag", data.flags.range, equalTo(false));
        assertThat("recall flag", data.flags.recall, equalTo(false));
        assertThat("ref flag", data.flags.ref, equalTo(false));
        assertThat("rel flag", data.flags.rel, equalTo(false));
        assertThat("rs232 flag", data.flags.rs232, equalTo(true));
        assertThat("store flag", data.flags.store, equalTo(false));
    }

}
