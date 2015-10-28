package com.dariancabot.protek608;

import com.dariancabot.protek608.exceptions.ProtocolException;


/**
 * The Decoder class is used to decode data packets from a Protek 608 DMM and update a provided {@link Data} Object with the aquired data.
 *
 * @author Darian Cabot
 */
public final class Decoder
{
    private final Data data;
    private EventListener eventListener;

    private static final byte packetStartByte = 0x5b;
    private static final byte packetEndByte = 0x5d;


    //-----------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param data the Data object to be used
     */
    public Decoder(Data data)
    {
        this.data = data;
    }


    private static final class BitMask
    {
        public static final byte NIBBLE_HIGH = (byte) 0b1111_0000;
        public static final byte NIBBLE_LOW = (byte) 0b0000_1111;

        // Dispaly digits / characters...
        public static final byte DIGIT = (byte) 0b1111_1110;
        public static final byte DIGIT_9 = (byte) 0b1101_1110;
        public static final byte DIGIT_8 = (byte) 0b1111_1110;
        public static final byte DIGIT_7 = (byte) 0b1000_1010;
        public static final byte DIGIT_6 = (byte) 0b1111_0110;
        public static final byte DIGIT_5 = (byte) 0b1101_0110;
        public static final byte DIGIT_4 = (byte) 0b0100_1110;
        public static final byte DIGIT_3 = (byte) 0b1001_1110;
        public static final byte DIGIT_2 = (byte) 0b1011_1100;
        public static final byte DIGIT_1 = (byte) 0b0000_1010;
        public static final byte DIGIT_1P1 = (byte) 0b0000_1011;
        public static final byte DIGIT_0 = (byte) 0b1111_1010;
        public static final byte DIGIT_L = (byte) 0b0111_0000;
        public static final byte DIGIT_P = (byte) 0b1110_1100;
        public static final byte DIGIT_E = (byte) 0b1111_0100;
        public static final byte DIGIT_N = (byte) 0b0010_0110;
        public static final byte DIGIT_H = (byte) 0b0110_0110;
        public static final byte DIGIT_R = (byte) 0b0010_0100;
        public static final byte DIGIT_T = (byte) 0b0111_0100;
        public static final byte DIGIT_A = (byte) 0b0110_1110;
        public static final byte DIGIT_D = (byte) 0b0011_1110;
        public static final byte DIGIT_BLANK = (byte) 0b0000_0000;

        // Main value...
        public static final byte MAIN_DECIMAL_P1 = (byte) 0b0000_0001;
        public static final byte MAIN_DECIMAL_P2 = (byte) 0b0001_0000;
        public static final byte MAIN_DECIMAL_P3 = (byte) 0b0001_0000;
        public static final byte MAIN_DECIMAL_P4 = (byte) 0b0001_0000;
        public static final byte MAIN_NEGATIVE = (byte) 0b0010_0000;
        public static final byte MAIN_KILO = (byte) 0b1000_0000;
        public static final byte MAIN_MEGA = (byte) 0b0000_1000;
        public static final byte MAIN_MICRO = (byte) 0b0000_0100;
        public static final byte MAIN_MILLI = (byte) 0b0000_0010;
        public static final byte MAIN_NANO = (byte) 0b0000_0001;
        public static final byte MAIN_HERTZ = (byte) 0b0100_0000;
        public static final byte MAIN_DEG_F = (byte) 0b0010_0000; // °F
        public static final byte MAIN_S_SM = (byte) 0b0001_0000; // Unknown 's' unit.
        public static final byte MAIN_OHM = (byte) 0b0000_0100;
        public static final byte MAIN_AMP = (byte) 0b0000_0010;
        public static final byte MAIN_FARAD = (byte) 0b0000_0001;
        public static final byte MAIN_VOLT = (byte) 0b0100_0000;
        public static final byte MAIN_S_LG = (byte) 0b0010_0000; // Siemens or Seconds.
        public static final byte MAIN_DEG_C = (byte) 0b0001_0000; // °C
        public static final byte MAIN_AC = (byte) 0b0100_0000;
        public static final byte MAIN_DC = (byte) 0b0001_0000;
        public static final byte MAIN_PW = (byte) 0b0001_0000; // Pulse width.

        // Bar graph...
        public static final byte BAR_GRAPH_0 = (byte) 0b1000_0000;
        // Note: The bar graph masks need to be int (not byte) as they are used in bit-shift operations to create a 16-bit value.
        public static final int BAR_GRAPH_1 = 0b0100_0000;
        public static final int BAR_GRAPH_2 = 0b0000_1000;
        public static final int BAR_GRAPH_4 = 0b0000_0100;
        public static final int BAR_GRAPH_8 = 0b0000_0010;
        public static final int BAR_GRAPH_16 = 0b0000_0001;
        public static final int BAR_GRAPH_32 = 0b0001_0000;
        public static final int BAR_GRAPH_64 = 0b0010_0000;
        public static final int BAR_GRAPH_128 = 0b0100_0000;
        public static final int BAR_GRAPH_256 = 0b1000_0000;
        public static final int BAR_GRAPH_512 = 0b0000_1000;
        public static final int BAR_GRAPH_1K = 0b0000_0100;
        public static final int BAR_GRAPH_2K = 0b0000_0010;
        public static final int BAR_GRAPH_4K = 0b0000_0001;
        public static final int BAR_GRAPH_8K = 0b0001_0000;
        public static final int BAR_GRAPH_16K = 0b0010_0000;

        // Sub value...
        public static final byte SUB_DECIMAL_P6 = (byte) 0b0001_0000;
        public static final byte SUB_DECIMAL_P7 = (byte) 0b0001_0000;
        public static final byte SUB_DECIMAL_P8 = (byte) 0b0001_0000;
        public static final byte SUB_DECIMAL_P9 = (byte) 0b0001_0000;
        public static final byte SUB_NEGATIVE = (byte) 0b0000_0010;
        public static final byte SUB_MILLI = (byte) 0b0000_0100;
        public static final byte SUB_GIGA = (byte) 0b0000_0010;
        public static final byte SUB_MEGA = (byte) 0b0000_0001;
        public static final byte SUB_KILO = (byte) 0b0001_0000;
        public static final byte SUB_PERCENT = (byte) 0b0000_1000; // %
        public static final byte SUB_DECIBEL_MW = (byte) 0b1000_0000; // dBm
        public static final byte SUB_VOLT = (byte) 0b0100_0000;
        public static final byte SUB_OHM = (byte) 0b0010_0000;
        public static final byte SUB_KELVIN = (byte) 0b0000_1000; // °K
        public static final byte SUB_AMP = (byte) 0b0000_0100;
        public static final byte SUB_HERTZ = (byte) 0b0000_0010;
        public static final byte SUB_AC = (byte) 0b0000_0100;
        public static final byte SUB_DC = (byte) 0b0000_0001;

        // Flags...
        public static final byte FLAG_AUTO_OFF = (byte) 0b0010_0000;
        public static final byte FLAG_PULSE = (byte) 0b1000_0000;
        public static final byte FLAG_MAXIMUM = (byte) 0b1000_0000;
        public static final byte FLAG_POS_PEAK = (byte) 0b0100_0000;
        public static final byte FLAG_RELATIVE = (byte) 0b0010_0000;
        public static final byte FLAG_RECALL = (byte) 0b0001_0000;
        public static final byte FLAG_GO_NG = (byte) 0b0000_0001;
        public static final byte FLAG_POS_PERCENT = (byte) 0b0000_1000;
        public static final byte FLAG_RS232C = (byte) 0b0001_0000;
        public static final byte FLAG_POSITIVE = (byte) 0b0000_0100;
        public static final byte FLAG_NEGATIVE = (byte) 0b0000_1000;
        public static final byte FLAG_MINIMUM = (byte) 0b0000_1000;
        public static final byte FLAG_NEG_PEAK = (byte) 0b0000_0100;
        public static final byte FLAG_AVERAGE = (byte) 0b0000_0010;
        public static final byte FLAG_STORE = (byte) 0b0000_0001;
        public static final byte FLAG_REFERENCE = (byte) 0b0000_0010;
        public static final byte FLAG_NEG_PERCENT = (byte) 0b0000_0100;
        public static final byte FLAG_LOW_BATTERY = (byte) 0b1000_0000;
        public static final byte FLAG_ZENER_DIODE = (byte) 0b1000_0000;
        public static final byte FLAG_RANGE = (byte) 0b0100_0000;
        public static final byte FLAG_HOLD = (byte) 0b0010_0000;
        public static final byte FLAG_DUTY = (byte) 0b0001_0000;
        public static final byte FLAG_CONTINUITY = (byte) 0b0000_1000;
    }


    //-----------------------------------------------------------------------
    /**
     * Decodes a Protek 608 packet, updates the Data object, and notifyies when complete using the EventListener.
     *
     * @param buffer The packet as a byte array. Must be 43 bytes long.
     *
     * @throws ProtocolException If the packet is invalid or unable to decode.
     */
    public void decodeSerialData(byte[] buffer) throws ProtocolException
    {
        // Check packet length.
        if (buffer.length != 43)
        {
            ProtocolException ex = new ProtocolException("Decode error: Packet length is " + buffer.length + ", but should be 43.");
            throw ex;
        }

        // Check for start byte of packet.
        if (buffer[0] != packetStartByte)
        {
            ProtocolException ex = new ProtocolException("Decode error: Packet start byte 0x5b not found at start of packet.");
            throw ex;
        }

        // Check for end byte of packet.
        if (buffer[42] != packetEndByte)
        {
            ProtocolException ex = new ProtocolException("Decode error: Packet end byte 0x5d not found at end of packet.");
            throw ex;
        }

        data.packetRaw = buffer; // Set the raw packet value.

        // Correct bit order of buffer and remove overhead (blank nibbles) to make workable packet data.
        byte[] packet = new byte[21];
        int byteCount = 0;
        byte lastByte = 0;

        for (int i = 0; i < buffer.length; i ++)
        {
            if (buffer[i] == packetStartByte) // Start of packet.
            {
                byteCount = 0;
            }
            else if (buffer[i] == packetEndByte) // End of packet.
            {
                data.packetTidy = packet; // Set the tidy packet value.
                decodePacket(packet); // Decode the packet.
                return;
            }
            else
            {
                int rawByte = buffer[i];

                // Protek nibbles are reverse order, correct that.
                rawByte = Integer.reverse(rawByte);

                // Only the last 4-bits/nibble is used for each raw byte.
                rawByte = (rawByte >> 28) & BitMask.NIBBLE_LOW; // Get the last 4 bits.

                byte thisByte = (byte) rawByte;

                if ((byteCount % 2) == 0)
                {
                    // Set lower nibble of new byte.
                    lastByte = thisByte;// Set lower nibble of new byte.
                }
                else
                {
                    // Set upper nibble of new byte.
                    byte fullByte = (byte) ((lastByte << 4) | thisByte);

                    // Put our new byte (2 combined nubbles) into the packet array.
                    packet[(byteCount - 1) / 2] = fullByte;
                }

                byteCount ++;
            }
        }
    }


    /**
     * Checks if a byte's bits match a mask.
     *
     * @param data The byte to check
     * @param mask The bit mask
     *
     * @return true if data's bits match the mask, otherwise false
     */
    private boolean checkMask(byte data, byte mask)
    {
        return (data & mask) == mask;
    }


    //-----------------------------------------------------------------------
    /**
     * Decodes a complete serial packet from the Protek 608 DMM. The decoded data will populate the provided Data object.
     *
     * @param packet
     *
     */
    private void decodePacket(byte[] packet)
    {

        // Main digit 4.
        byte digit4Bits = (byte) ((packet[5] << 4) & BitMask.NIBBLE_HIGH);
        digit4Bits |= (byte) ((packet[6] >> 4) & BitMask.NIBBLE_LOW);
        digit4Bits &= (byte) BitMask.DIGIT;

        // Main digit 3.
        byte digit3Bits = (byte) ((packet[6] << 4) & BitMask.NIBBLE_HIGH);
        digit3Bits |= (byte) ((packet[7] >> 4) & BitMask.NIBBLE_LOW);
        digit3Bits &= (byte) BitMask.DIGIT;

        // Main digit 2.
        byte digit2Bits = (byte) ((packet[7] << 4) & BitMask.NIBBLE_HIGH);
        digit2Bits |= (byte) ((packet[8] >> 4) & BitMask.NIBBLE_LOW);
        digit2Bits &= (byte) BitMask.DIGIT;

        // Main digit 1.
        byte digit1Bits = (byte) packet[11];
        digit1Bits &= (byte) BitMask.DIGIT;

        // Main digit 0.
        byte digit0Bits = (byte) packet[12];
        digit0Bits &= (byte) BitMask.DIGIT;

        String mainDigits = decodeDigit(digit4Bits);

        // Decimal place P4 (left-most)...
        if (checkMask(packet[6], BitMask.MAIN_DECIMAL_P4))
        {
            mainDigits += ".";
        }

        mainDigits += decodeDigit(digit3Bits);

        // Decimal place P3...
        if (checkMask(packet[7], BitMask.MAIN_DECIMAL_P3))
        {
            mainDigits += ".";
        }

        mainDigits += decodeDigit(digit2Bits);

        // Decimal place P2...
        if (checkMask(packet[8], BitMask.MAIN_DECIMAL_P2))
        {
            mainDigits += ".";
        }

        mainDigits += decodeDigit(digit1Bits);

        // Decimal place P1...
        if (checkMask(packet[11], BitMask.MAIN_DECIMAL_P1))
        {
            mainDigits += ".";
        }

        mainDigits += decodeDigit(digit0Bits); // Digit 0 (right-most).

        // Main-digit negative sign...
        if (checkMask(packet[5], BitMask.MAIN_NEGATIVE))
        {
            mainDigits = "-" + mainDigits;
        }
        else
        {
            mainDigits = " " + mainDigits;
        }

        // Set main value.
        data.mainValue.setValue(mainDigits);

        // Main value unit prefix.
        data.mainValue.unit.setPrefix(Data.Value.Unit.Prefix.NONE);

        if (checkMask(packet[14], BitMask.MAIN_KILO))
        {
            data.mainValue.unit.setPrefix(Data.Value.Unit.Prefix.KILO);
        }

        if (checkMask(packet[14], BitMask.MAIN_MEGA))
        {
            data.mainValue.unit.setPrefix(Data.Value.Unit.Prefix.MEGA);
        }

        if (checkMask(packet[14], BitMask.MAIN_MICRO))
        {
            data.mainValue.unit.setPrefix(Data.Value.Unit.Prefix.MICRO);
        }

        if (checkMask(packet[14], BitMask.MAIN_MILLI))
        {
            data.mainValue.unit.setPrefix(Data.Value.Unit.Prefix.MILLI);
        }

        if (checkMask(packet[14], BitMask.MAIN_NANO))
        {
            data.mainValue.unit.setPrefix(Data.Value.Unit.Prefix.NANO);
        }

        // Main value unit measurement.
        data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.NONE);

        if (checkMask(packet[13], BitMask.MAIN_HERTZ))
        {
            data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.HERTZ);
        }

        if (checkMask(packet[13], BitMask.MAIN_DEG_F))
        {
            data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.DEG_F);
        }

        // The (lowercase) 's' unit is on the DMM's LCD, however I haven't seen it used and I don't know it's meaning.
        // It's included here so the packet is completly decoded, but it's meaning is unknown. Can anyone enlighten me?
        if (checkMask(packet[13], BitMask.MAIN_S_SM))
        {
            data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.S);
        }

        if (checkMask(packet[13], BitMask.MAIN_OHM))
        {
            data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.OHM);
        }

        if (checkMask(packet[13], BitMask.MAIN_AMP))
        {
            data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.AMPERE);
        }

        if (checkMask(packet[13], BitMask.MAIN_FARAD))
        {
            data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.FARAD);
        }

        if (checkMask(packet[14], BitMask.MAIN_VOLT))
        {
            data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.VOLT);
        }

        if (checkMask(packet[14], BitMask.MAIN_S_LG))
        {
            // There are two units that use the same 'S' symbol on the display.
            // Determine the correct one by checking for pulse width (seconds).

            if (checkMask(packet[4], BitMask.MAIN_PW))
            {
                // Pulse width, so in this case 'S' means SECOND.
                data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.SECOND);
            }
            else
            {
                // ... otherwise, 'S' means SIEMENS.
                data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.SIEMENS);
            }
        }

        if (checkMask(packet[14], BitMask.MAIN_DEG_C))
        {
            data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.DEG_C);
        }

        // Main value unit type.
        data.mainValue.unit.setType(Data.Value.Unit.Type.NONE); // Clear.

        if (checkMask(packet[5], BitMask.MAIN_AC))
        {
            data.mainValue.unit.setType(Data.Value.Unit.Type.AC);
        }

        if (checkMask(packet[5], BitMask.MAIN_DC))
        {
            data.mainValue.unit.setType(Data.Value.Unit.Type.DC);
        }

        if (checkMask(packet[4], BitMask.MAIN_PW))
        {
            data.mainValue.unit.setType(Data.Value.Unit.Type.PW);
        }

        // Sub digits...
        byte digit9Bits = (byte) ((packet[2] << 4) & BitMask.NIBBLE_HIGH);
        digit9Bits |= (byte) ((packet[2] >> 4) & BitMask.NIBBLE_LOW);
        digit9Bits &= (byte) BitMask.DIGIT;

        byte digit8Bits = (byte) ((packet[1] << 4) & BitMask.NIBBLE_HIGH);
        digit8Bits |= (byte) ((packet[1] >> 4) & BitMask.NIBBLE_LOW);
        digit8Bits &= (byte) BitMask.DIGIT;

        byte digit7Bits = (byte) ((packet[0] << 4) & BitMask.NIBBLE_HIGH);
        digit7Bits |= (byte) ((packet[0] >> 4) & BitMask.NIBBLE_LOW);
        digit7Bits &= (byte) BitMask.DIGIT;

        byte digit6Bits = (byte) ((packet[19] << 4) & BitMask.NIBBLE_HIGH);
        digit6Bits |= (byte) ((packet[19] >> 4) & BitMask.NIBBLE_LOW);
        digit6Bits &= (byte) BitMask.DIGIT;

        byte digit5Bits = (byte) ((packet[18] << 4) & BitMask.NIBBLE_HIGH);
        digit5Bits |= (byte) ((packet[18] >> 4) & BitMask.NIBBLE_LOW);
        digit5Bits &= (byte) BitMask.DIGIT;

        String subDigits = decodeDigit(digit9Bits); // Digit 9 (left-most).

        // Decimal place P9 (left-most)...
        if (checkMask(packet[2], BitMask.SUB_DECIMAL_P9))
        {
            subDigits += ".";
        }

        subDigits += decodeDigit(digit8Bits);

        // Decimal place P8...
        if (checkMask(packet[1], BitMask.SUB_DECIMAL_P8))
        {
            subDigits += ".";
        }

        subDigits += decodeDigit(digit7Bits);

        // Decimal place P7...
        if (checkMask(packet[0], BitMask.SUB_DECIMAL_P7))
        {
            subDigits += ".";
        }

        subDigits += decodeDigit(digit6Bits);

        // Decimal place P6...
        if (checkMask(packet[19], BitMask.SUB_DECIMAL_P6))
        {
            subDigits += ".";
        }

        subDigits += decodeDigit(digit5Bits); // Digit 5 (right-most).

        // Sub-digit negative sign...
        if (checkMask(packet[3], BitMask.SUB_NEGATIVE))
        {
            subDigits = "-" + subDigits;
        }
        else
        {
            subDigits = " " + subDigits;
        }

        // Set sub value.
        data.subValue.setValue(subDigits);

        // Sub value unit prefix.
        data.subValue.unit.setPrefix(Data.Value.Unit.Prefix.NONE);

        if (checkMask(packet[16], BitMask.SUB_MILLI))
        {
            data.subValue.unit.setPrefix(Data.Value.Unit.Prefix.MILLI);
        }

        if (checkMask(packet[16], BitMask.SUB_GIGA))
        {
            data.subValue.unit.setPrefix(Data.Value.Unit.Prefix.GIGA);
        }

        if (checkMask(packet[16], BitMask.SUB_MEGA))
        {
            data.subValue.unit.setPrefix(Data.Value.Unit.Prefix.MEGA);
        }

        if (checkMask(packet[17], BitMask.SUB_KILO))
        {
            data.subValue.unit.setPrefix(Data.Value.Unit.Prefix.KILO);
        }

        // Sub value unit measurement.
        data.subValue.unit.setMeasurement(Data.Value.Unit.Measurement.NONE);

        if (checkMask(packet[16], BitMask.SUB_PERCENT))
        {
            data.subValue.unit.setMeasurement(Data.Value.Unit.Measurement.PERCENT);
        }

        if (checkMask(packet[17], BitMask.SUB_DECIBEL_MW))
        {
            data.subValue.unit.setMeasurement(Data.Value.Unit.Measurement.DECIBEL_MW);
        }

        if (checkMask(packet[17], BitMask.SUB_VOLT))
        {
            data.subValue.unit.setMeasurement(Data.Value.Unit.Measurement.VOLT);
        }

        if (checkMask(packet[17], BitMask.SUB_OHM))
        {
            data.subValue.unit.setMeasurement(Data.Value.Unit.Measurement.OHM);
        }

        if (checkMask(packet[17], BitMask.SUB_KELVIN))
        {
            data.subValue.unit.setMeasurement(Data.Value.Unit.Measurement.KELVIN);
        }

        if (checkMask(packet[17], BitMask.SUB_AMP))
        {
            data.subValue.unit.setMeasurement(Data.Value.Unit.Measurement.AMPERE);
        }

        if (checkMask(packet[17], BitMask.SUB_HERTZ))
        {
            data.subValue.unit.setMeasurement(Data.Value.Unit.Measurement.HERTZ);
        }

        // Sub value unit type
        data.subValue.unit.setType(Data.Value.Unit.Type.NONE); // Clear.

        if (checkMask(packet[3], BitMask.SUB_AC))
        {
            data.subValue.unit.setType(Data.Value.Unit.Type.AC);
        }

        if (checkMask(packet[3], BitMask.SUB_DC))
        {
            data.subValue.unit.setType(Data.Value.Unit.Type.DC);
        }

        // Bar graph...
        Integer barGraph = null;

        // Check if Bar Graph 0 segment active (i.e. bar graph displayed).
        if (checkMask(packet[4], BitMask.BAR_GRAPH_0))
        {
            // Bar graph displayed, so check all segments...
            barGraph = 0;
            barGraph += ((packet[4] & BitMask.BAR_GRAPH_1) >> 6);
            barGraph += ((packet[4] & BitMask.BAR_GRAPH_2) >> 2);
            barGraph += (packet[4] & BitMask.BAR_GRAPH_4);
            barGraph += ((packet[4] & BitMask.BAR_GRAPH_8) << 2);
            barGraph += ((packet[4] & BitMask.BAR_GRAPH_16) << 4);
            barGraph += ((packet[16] & BitMask.BAR_GRAPH_32) << 1);
            barGraph += ((packet[16] & BitMask.BAR_GRAPH_64) << 1);
            barGraph += ((packet[16] & BitMask.BAR_GRAPH_128) << 1);
            barGraph += ((packet[16] & BitMask.BAR_GRAPH_256) << 1);
            barGraph += ((packet[15] & BitMask.BAR_GRAPH_512) << 6);
            barGraph += ((packet[15] & BitMask.BAR_GRAPH_1K) << 8);
            barGraph += ((packet[15] & BitMask.BAR_GRAPH_2K) << 10);
            barGraph += ((packet[15] & BitMask.BAR_GRAPH_4K) << 12);
            barGraph += ((packet[15] & BitMask.BAR_GRAPH_8K) << 9);
            barGraph += ((packet[15] & BitMask.BAR_GRAPH_16K) << 9);
        }

        data.barGraph = barGraph;

        // Set annunciators...
        data.annunciators.autoOff = checkMask(packet[9], BitMask.FLAG_AUTO_OFF);
        data.annunciators.pulse = checkMask(packet[9], BitMask.FLAG_PULSE);
        data.annunciators.maximum = checkMask(packet[10], BitMask.FLAG_MAXIMUM);
        data.annunciators.posPeak = checkMask(packet[10], BitMask.FLAG_POS_PEAK);
        data.annunciators.relative = checkMask(packet[10], BitMask.FLAG_RELATIVE);
        data.annunciators.recall = checkMask(packet[10], BitMask.FLAG_RECALL);
        data.annunciators.goNg = checkMask(packet[10], BitMask.FLAG_GO_NG);
        data.annunciators.posPercent = checkMask(packet[10], BitMask.FLAG_POS_PERCENT);
        data.annunciators.rs232c = checkMask(packet[9], BitMask.FLAG_RS232C);
        data.annunciators.positive = checkMask(packet[8], BitMask.FLAG_POSITIVE);
        data.annunciators.negative = checkMask(packet[8], BitMask.FLAG_NEGATIVE);
        data.annunciators.minimum = checkMask(packet[9], BitMask.FLAG_MINIMUM);
        data.annunciators.negPeak = checkMask(packet[9], BitMask.FLAG_NEG_PEAK);
        data.annunciators.average = checkMask(packet[9], BitMask.FLAG_AVERAGE);
        data.annunciators.store = checkMask(packet[9], BitMask.FLAG_STORE);
        data.annunciators.reference = checkMask(packet[10], BitMask.FLAG_REFERENCE);
        data.annunciators.negPercent = checkMask(packet[10], BitMask.FLAG_NEG_PERCENT);
        data.annunciators.lowBattery = checkMask(packet[5], BitMask.FLAG_LOW_BATTERY);

        // The following are refered to as sub (value) units in the manual, but they seem like global?
        data.annunciators.zenerDiode = checkMask(packet[3], BitMask.FLAG_ZENER_DIODE);
        data.annunciators.range = checkMask(packet[3], BitMask.FLAG_RANGE);
        data.annunciators.hold = checkMask(packet[3], BitMask.FLAG_HOLD);
        data.annunciators.duty = checkMask(packet[3], BitMask.FLAG_DUTY);
        data.annunciators.continuity = checkMask(packet[3], BitMask.FLAG_CONTINUITY);

        // Notify using the event listener if one is set.
        if (eventListener != null)
        {
            eventListener.dataUpdateEvent();
        }
    }


    //-----------------------------------------------------------------------
    /**
     * Decodes a single LCD digit.
     *
     * @param encoded the value of the digit (nominally 7-bits from protocol).
     *
     * @return A String representation of the digit value, either numerical or otherwise.
     */
    private String decodeDigit(byte digit)
    {
        // The order that the masks are checked is important! Rearanging this should be done with care.
        if ((digit | BitMask.DIGIT_BLANK) == BitMask.DIGIT_BLANK)
        {
            return " ";
        }
        else if (checkMask(digit, BitMask.DIGIT_8))
        {
            return "8";
        }
        else if (checkMask(digit, BitMask.DIGIT_A))
        {
            return "A";
        }
        else if (checkMask(digit, BitMask.DIGIT_9))
        {
            return "9";
        }
        else if (checkMask(digit, BitMask.DIGIT_6))
        {
            return "6";
        }
        else if (checkMask(digit, BitMask.DIGIT_5))
        {
            return "5";
        }
        else if (checkMask(digit, BitMask.DIGIT_4))
        {
            return "4";
        }
        else if (checkMask(digit, BitMask.DIGIT_3))
        {
            return "3";
        }
        else if (checkMask(digit, BitMask.DIGIT_2))
        {
            return "2";
        }
        else if (checkMask(digit, BitMask.DIGIT_D))
        {
            return "d";
        }
        else if (checkMask(digit, BitMask.DIGIT_P))
        {
            return "P";
        }
        else if (checkMask(digit, BitMask.DIGIT_E))
        {
            return "E";
        }
        else if (checkMask(digit, BitMask.DIGIT_H))
        {
            return "h";
        }
        else if (checkMask(digit, BitMask.DIGIT_N))
        {
            return "n";
        }
        else if (checkMask(digit, BitMask.DIGIT_T))
        {
            return "t";
        }
        else if (checkMask(digit, BitMask.DIGIT_R))
        {
            return "r";
        }
        else if ((checkMask((byte)  ~ digit, (byte)  ~ BitMask.DIGIT_1)) | (checkMask((byte)  ~ digit, (byte)  ~ BitMask.DIGIT_1P1)))
        {
            return "1";
        }
        else if (checkMask(digit, BitMask.DIGIT_0))
        {
            return "0";
        }
        else if (checkMask(digit, BitMask.DIGIT_L))
        {
            return "L";
        }
        else if (checkMask(digit, BitMask.DIGIT_7))
        {
            return "7";
        }
        else
        {
            return "?";
        }
    }


    //-----------------------------------------------------------------------
    /**
     * Sets an EventListener to be notified when data is received over the Serial Port.
     *
     * @param eventListener An EventListener Object to be notified when data is received
     */
    public void setEventListener(EventListener eventListener)
    {
        this.eventListener = eventListener;
    }

}
