package com.dariancabot.protek608;

import com.dariancabot.protek608.exceptions.ProtocolException;


/**
 *
 * @author Darian Cabot
 */
public final class Decoder
{
    private final Data data;
    private EventListener eventListener;

    private final byte packetStartByte = 0x5b;
    private final byte packetEndByte = 0x5d;


    //-----------------------------------------------------------------------
    /**
     * Creates a new Decoder instance.
     *
     * @param data the Data object to be used
     */
    public Decoder(Data data)
    {
        this.data = data;
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
        if (buffer[0] != 0x5b)
        {
            ProtocolException ex = new ProtocolException("Decode error: Packet start byte 0x5b not found at start of packet.");
            throw ex;
        }

        // Check for end byte of packet.
        if (buffer[42] != 0x5d)
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
                rawByte = (rawByte >> 28) & 0b0000_1111; // Get the last 4 bits.

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


    //-----------------------------------------------------------------------
    /**
     * Decodes a complete serial packet from the Protek 608 DMM. The decoded data will populate the provided Data object.
     *
     * @param packet
     *
     */
    private void decodePacket(byte[] packet)
    {
        byte digitMask = (byte) 0b1111_1110;
        byte highNibbleMask = (byte) 0b1111_0000;
        byte lowNibbleMask = (byte) 0b0000_1111;
        byte decimalMask = (byte) 0b0001_0000;

        // Main digit 4.
        byte digit4Bits = (byte) ((packet[5] << 4) & highNibbleMask);
        digit4Bits |= (byte) ((packet[6] >> 4) & lowNibbleMask);
        digit4Bits &= (byte) digitMask;

        // Main digit 3.
        byte digit3Bits = (byte) ((packet[6] << 4) & highNibbleMask);
        digit3Bits |= (byte) ((packet[7] >> 4) & lowNibbleMask);
        digit3Bits &= (byte) digitMask;

        // Main digit 2.
        byte digit2Bits = (byte) ((packet[7] << 4) & highNibbleMask);
        digit2Bits |= (byte) ((packet[8] >> 4) & lowNibbleMask);
        digit2Bits &= (byte) digitMask;

        // Main digit 1.
        byte digit1Bits = (byte) packet[11];

        // Main digit 0.
        byte digit0Bits = (byte) packet[12];

        String mainDigits = decodeDigit(digit4Bits);

        // Decimal place P4 (left-most)...
        if ((packet[6] & decimalMask) == decimalMask)
        {
            mainDigits += ".";
        }

        mainDigits += decodeDigit(digit3Bits);

        // Decimal place P3...
        if ((packet[7] & decimalMask) == decimalMask)
        {
            mainDigits += ".";
        }

        mainDigits += decodeDigit(digit2Bits);

        // Decimal place P2...
        if ((packet[8] & decimalMask) == decimalMask)
        {
            mainDigits += ".";
        }

        mainDigits += decodeDigit(digit1Bits);

        // Decimal place P1...
        if ((packet[11] & 0b0000_0001) == 0b0000_0001)
        {
            mainDigits += ".";
        }

        mainDigits += decodeDigit(digit0Bits); // Digit 0 (right-most).

        // Main-digit negative sign...
        if ((packet[5] & 0b0010_0000) == 0b0010_0000)
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

        if ((packet[14] & 0b1000_0000) == 0b1000_0000) // k
        {
            data.mainValue.unit.setPrefix(Data.Value.Unit.Prefix.KILO);
        }

        if ((packet[14] & 0b0000_1000) == 0b0000_1000) // M
        {
            data.mainValue.unit.setPrefix(Data.Value.Unit.Prefix.MEGA);
        }

        if ((packet[14] & 0b0000_0100) == 0b0000_0100) // u
        {
            data.mainValue.unit.setPrefix(Data.Value.Unit.Prefix.MICRO);
        }

        if ((packet[14] & 0b0000_0010) == 0b0000_0010) // m
        {
            data.mainValue.unit.setPrefix(Data.Value.Unit.Prefix.MILLI);
        }

        if ((packet[14] & 0b0000_0001) == 0b0000_0001) // n
        {
            data.mainValue.unit.setPrefix(Data.Value.Unit.Prefix.NANO);
        }

        // Main value unit measurement.
        data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.NONE);

        if ((packet[13] & 0b0100_0000) == 0b0100_0000) // Hz
        {
            data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.HERTZ);
        }

        if ((packet[13] & 0b0010_0000) == 0b0010_0000) // °F
        {
            data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.DEG_F);
        }

        if ((packet[13] & 0b0001_0000) == 0b0001_0000) // s
        {
            data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.S);
        }

        if ((packet[13] & 0b0000_0100) == 0b0000_0100) // ohm
        {
            data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.OHM);
        }

        if ((packet[13] & 0b0000_0010) == 0b0000_0010) // A
        {
            data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.AMPERE);
        }

        if ((packet[13] & 0b0000_0001) == 0b0000_0001) // F
        {
            data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.FARAD);
        }

        if ((packet[14] & 0b0100_0000) == 0b0100_0000) // V
        {
            data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.VOLT);
        }

        if ((packet[14] & 0b0010_0000) == 0b0010_0000) // S
        {
            // There are two units that use the same 'S' symbol on the display.
            // Determine the correct one by checking for pulse width (seconds).

            if ((packet[4] & 0b0001_0000) == 0b0001_0000) // PW
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

        if ((packet[14] & 0b0001_0000) == 0b0001_0000) // °C
        {
            data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.DEG_C);
        }

        // Main value unit type.
        data.mainValue.unit.setType(Data.Value.Unit.Type.NONE); // Clear.

        if ((packet[5] & 0b0100_0000) == 0b0100_0000) // AC
        {
            data.mainValue.unit.setType(Data.Value.Unit.Type.AC);
        }

        if ((packet[5] & 0b0001_0000) == 0b0001_0000) // DC
        {
            data.mainValue.unit.setType(Data.Value.Unit.Type.DC);
        }

        if ((packet[4] & 0b0001_0000) == 0b0001_0000) // PW
        {
            data.mainValue.unit.setType(Data.Value.Unit.Type.PW);
        }

        // Sub digits...
        byte digit9Bits = (byte) ((packet[2] << 4) & highNibbleMask);
        digit9Bits |= (byte) ((packet[2] >> 4) & lowNibbleMask);
        digit9Bits &= (byte) digitMask;

        byte digit8Bits = (byte) ((packet[1] << 4) & highNibbleMask);
        digit8Bits |= (byte) ((packet[1] >> 4) & lowNibbleMask);
        digit8Bits &= (byte) digitMask;

        byte digit7Bits = (byte) ((packet[0] << 4) & highNibbleMask);
        digit7Bits |= (byte) ((packet[0] >> 4) & lowNibbleMask);
        digit7Bits &= (byte) digitMask;

        byte digit6Bits = (byte) ((packet[19] << 4) & highNibbleMask);
        digit6Bits |= (byte) ((packet[19] >> 4) & lowNibbleMask);
        digit6Bits &= (byte) digitMask;

        byte digit5Bits = (byte) ((packet[18] << 4) & highNibbleMask);
        digit5Bits |= (byte) ((packet[18] >> 4) & lowNibbleMask);
        digit5Bits &= (byte) digitMask;

        String subDigits = decodeDigit(digit9Bits); // Digit 9 (left-most).

        // Decimal place P9 (left-most)...
        if ((packet[2] & decimalMask) == decimalMask)
        {
            subDigits += ".";
        }

        subDigits += decodeDigit(digit8Bits);

        // Decimal place P8...
        if ((packet[1] & decimalMask) == decimalMask)
        {
            subDigits += ".";
        }

        subDigits += decodeDigit(digit7Bits);

        // Decimal place P7...
        if ((packet[0] & decimalMask) == decimalMask)
        {
            subDigits += ".";
        }

        subDigits += decodeDigit(digit6Bits);

        // Decimal place P6...
        if ((packet[19] & decimalMask) == decimalMask)
        {
            subDigits += ".";
        }

        subDigits += decodeDigit(digit5Bits); // Digit 5 (right-most).

        // Sub-digit negative sign...
        if ((packet[3] & 0b0000_0010) == 0b0000_0010)
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

        if ((packet[16] & 0b0000_0100) == 0b0000_0100) // m
        {
            data.subValue.unit.setPrefix(Data.Value.Unit.Prefix.MILLI);
        }

        if ((packet[16] & 0b0000_0010) == 0b0000_0010) // G
        {
            data.subValue.unit.setPrefix(Data.Value.Unit.Prefix.GIGA);
        }

        if ((packet[16] & 0b0000_0001) == 0b0000_0001) // M
        {
            data.subValue.unit.setPrefix(Data.Value.Unit.Prefix.MEGA);
        }

        if ((packet[17] & 0b0001_0000) == 0b0001_0000) // k
        {
            data.subValue.unit.setPrefix(Data.Value.Unit.Prefix.KILO);
        }

        // Sub value unit measurement.
        data.subValue.unit.setMeasurement(Data.Value.Unit.Measurement.NONE);

        if ((packet[16] & 0b0000_1000) == 0b0000_1000) // %
        {
            data.subValue.unit.setMeasurement(Data.Value.Unit.Measurement.PERCENT);
        }

        if ((packet[17] & 0b1000_0000) == 0b1000_0000) // dBm
        {
            data.subValue.unit.setMeasurement(Data.Value.Unit.Measurement.DECIBEL_MW);
        }

        if ((packet[17] & 0b0100_0000) == 0b0100_0000) // V
        {
            data.subValue.unit.setMeasurement(Data.Value.Unit.Measurement.VOLT);
        }

        if ((packet[17] & 0b0010_0000) == 0b0010_0000) // ohm
        {
            data.subValue.unit.setMeasurement(Data.Value.Unit.Measurement.OHM);
        }

        if ((packet[17] & 0b0000_1000) == 0b0000_1000) // °K
        {
            data.subValue.unit.setMeasurement(Data.Value.Unit.Measurement.KELVIN);
        }

        if ((packet[17] & 0b0000_0100) == 0b0000_0100) // A
        {
            data.subValue.unit.setMeasurement(Data.Value.Unit.Measurement.AMPERE);
        }

        if ((packet[17] & 0b0000_0010) == 0b0000_0010) // Hz
        {
            data.subValue.unit.setMeasurement(Data.Value.Unit.Measurement.HERTZ);
        }

        // Sub value unit type
        data.subValue.unit.setType(Data.Value.Unit.Type.NONE); // Clear.

        if ((packet[3] & 0b0000_0100) == 0b0000_0100) // AC
        {
            data.subValue.unit.setType(Data.Value.Unit.Type.AC);
        }

        if ((packet[3] & 0b0000_0001) == 0b0000_0001) // DC
        {
            data.subValue.unit.setType(Data.Value.Unit.Type.DC);
        }

        // Bar graph...
        Integer barGraph = null;

        if ((packet[4] & 0b1000_0000) == 0b1000_0000) // Bar graph '0' set.
        {
            // Bar graph displayed, so check all segments...
            barGraph = 0;
            barGraph += ((packet[4] & 0b0100_0000) >> 6); // B1.
            barGraph += ((packet[4] & 0b0000_1000) >> 2); // B2.
            barGraph += (packet[4] & 0b0000_0100); // B4.
            barGraph += ((packet[4] & 0b0000_0010) << 2); // B8.
            barGraph += ((packet[4] & 0b0000_0001) << 4); // B16.
            barGraph += ((packet[16] & 0b0001_0000) << 1); // B32.
            barGraph += ((packet[16] & 0b0010_0000) << 1); // B64.
            barGraph += ((packet[16] & 0b0100_0000) << 1); // B128.
            barGraph += ((packet[16] & 0b1000_0000) << 1); // B256.
            barGraph += ((packet[15] & 0b0000_1000) << 6); // B512.
            barGraph += ((packet[15] & 0b0000_0100) << 8); // B1K.
            barGraph += ((packet[15] & 0b0000_0010) << 10); // B2K.
            barGraph += ((packet[15] & 0b0000_0001) << 12); // B4K.
            barGraph += ((packet[15] & 0b0001_0000) << 9); // B8K.
            barGraph += ((packet[15] & 0b0010_0000) << 9); // B16K.
        }

        data.barGraph = barGraph;

        // Set flags...
        data.flags.autoOff = (packet[9] & 0b0010_0000) == 0b0010_0000;
        data.flags.pulse = (packet[9] & 0b1000_0000) == 0b1000_0000;
        data.flags.max = (packet[10] & 0b1000_0000) == 0b1000_0000;
        data.flags.posPeak = (packet[10] & 0b0100_0000) == 0b0100_0000;
        data.flags.rel = (packet[10] & 0b0010_0000) == 0b0010_0000;
        data.flags.recall = (packet[10] & 0b0001_0000) == 0b0001_0000;
        data.flags.goNg = (packet[10] & 0b0000_0001) == 0b0000_0001;
        data.flags.posPercent = (packet[10] & 0b0000_1000) == 0b0000_1000;
        data.flags.rs232 = (packet[9] & 0b0001_0000) == 0b0001_0000;
        data.flags.pos = (packet[8] & 0b0000_0100) == 0b0000_0100;
        data.flags.neg = (packet[8] & 0b0000_1000) == 0b0000_1000;
        data.flags.min = (packet[9] & 0b0000_1000) == 0b0000_1000;
        data.flags.negPeak = (packet[9] & 0b0000_0100) == 0b0000_0100;
        data.flags.avg = (packet[9] & 0b0000_0010) == 0b0000_0010;
        data.flags.store = (packet[9] & 0b0000_0001) == 0b0000_0001;
        data.flags.ref = (packet[10] & 0b0000_0010) == 0b0000_0010;
        data.flags.negPercent = (packet[10] & 0b0000_0100) == 0b0000_0100;
        data.flags.lowBattery = (packet[5] & 0b1000_0000) == 0b1000_0000;

        // The following are refered to as sub (value) units in the manual,
        // but they seem like global?
        data.flags.diode = (packet[3] & 0b1000_0000) == 0b1000_0000;
        data.flags.range = (packet[3] & 0b0100_0000) == 0b0100_0000;
        data.flags.hold = (packet[3] & 0b0010_0000) == 0b0010_0000;
        data.flags.duty = (packet[3] & 0b0001_0000) == 0b0001_0000;
        data.flags.audio = (packet[3] & 0b0000_1000) == 0b0000_1000;

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
        // Define the masks to be checked.
        byte mask9 = (byte) 0b1101_1110;
        byte mask8 = (byte) 0b1111_1110;
        byte mask7 = (byte) 0b1000_1010;
        byte mask6 = (byte) 0b1111_0110;
        byte mask5 = (byte) 0b1101_0110;
        byte mask4 = (byte) 0b0100_1110;
        byte mask3 = (byte) 0b1001_1110;
        byte mask2 = (byte) 0b1011_1100;
        byte mask1 = (byte) 0b0000_1010;
        byte mask1P1 = (byte) 0b0000_1011;
        byte mask0 = (byte) 0b1111_1010;
        byte maskL = (byte) 0b0111_0000;
        byte maskP = (byte) 0b1110_1100;
        byte maskE = (byte) 0b1111_0100;
        byte maskN = (byte) 0b0010_0110;
        byte maskH = (byte) 0b0110_0110;
        byte maskR = (byte) 0b0010_0100;
        byte maskT = (byte) 0b0111_0100;
        byte maskA = (byte) 0b0110_1110;
        byte maskD = (byte) 0b0011_1110;
        byte maskBlank = (byte) 0b0000_0000;

        // The order that the masks are checked is important! Rearanging this should be done with care.
        if ((digit | maskBlank) == maskBlank)
        {
            return " ";
        }
        else if ((digit & mask8) == mask8)
        {
            return "8";
        }
        else if ((digit & maskA) == maskA)
        {
            return "A";
        }
        else if ((digit & mask9) == mask9)
        {
            return "9";
        }
        else if ((digit & mask6) == mask6)
        {
            return "6";
        }
        else if ((digit & mask5) == mask5)
        {
            return "5";
        }
        else if ((digit & mask4) == mask4)
        {
            return "4";
        }
        else if ((digit & mask3) == mask3)
        {
            return "3";
        }
        else if ((digit & mask2) == mask2)
        {
            return "2";
        }
        else if ((digit & maskD) == maskD)
        {
            return "d";
        }
        else if ((digit & maskP) == maskP)
        {
            return "P";
        }
        else if ((digit & maskE) == maskE)
        {
            return "E";
        }
        else if ((digit & maskH) == maskH)
        {
            return "h";
        }
        else if ((digit & maskN) == maskN)
        {
            return "n";
        }
        else if ((digit & maskT) == maskT)
        {
            return "t";
        }
        else if ((digit & maskR) == maskR)
        {
            return "r";
        }
        else if ((( ~ digit &  ~ mask1) ==  ~ mask1) || ( ~ digit &  ~ mask1P1) ==  ~ mask1P1)
        {
            return "1";
        }
        else if ((digit & mask0) == mask0)
        {
            return "0";
        }
        else if ((digit & maskL) == maskL)
        {
            return "L";
        }
        else if ((digit & mask7) == mask7)
        {
            return "7";
        }
        else
        {
            return "?";
        }
    }


    public void setEventListener(EventListener eventListener)
    {
        this.eventListener = eventListener;
    }

}
