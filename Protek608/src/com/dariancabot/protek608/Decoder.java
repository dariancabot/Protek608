package com.dariancabot.protek608;

import com.dariancabot.protek608.exceptions.ProtocolException;


/**
 *
 * @author Darian Cabot
 */
public final class Decoder
{

    private final int[] packet = new int[21];
    private final Data data;
    private EventListener eventListener;


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
        int byteCount = 0;
        int idata;
        int lastByte = 0;

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

        for (int i = 0; i < buffer.length; i ++)
        {
            if (buffer[i] == 0x5b) // Start of packet.
            {
                byteCount = 0;
            }
            else if (buffer[i] == 0x5d) // End of packet.
            {
                decodePacket();
            }
            else
            {
                idata = buffer[i];

                // Protek nibbles are reverse order, correct that.
                idata = Integer.reverse(idata);

                // Only the last 4-bits/nibble is used for each 8-bit byte.
                // Discard and rebuild a more concise packet to work with.
                idata = idata >> 28;
                int mask = 0b1111;
                idata = idata & mask;

                if ((byteCount % 2) == 0)
                {
                    // Set lower nibble of new byte.
                    lastByte = idata;
                }
                else
                {
                    // Set upper nibble of new byte.
                    lastByte = lastByte << 4;
                    idata = lastByte | idata;
                    packet[(byteCount - 1) / 2] = idata;
                }

                byteCount ++;
            }
        }
    }


    //-----------------------------------------------------------------------
    /**
     * Decodes a complete serial packet from the Protek 608 DMM. The decoded data will populate the provided Data object.
     */
    private void decodePacket()
    {
        int decimalMask = 0b00010000;

        // Main digits...
        int digit4Bits = (((packet[5] << 4) | (packet[6] >> 4)) & 0b11111110);
        int digit3Bits = (((packet[6] << 4) | (packet[7] >> 4)) & 0b11111110);
        int digit2Bits = (((packet[7] << 4) | (packet[8] >> 4)) & 0b11111110);
        int digit1Bits = packet[11];
        int digit0Bits = packet[12];

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
        if ((packet[11] & 0b00000001) == 0b00000001)
        {
            mainDigits += ".";
        }

        mainDigits += decodeDigit(digit0Bits); // Digit 0 (right-most).

        // Main-digit negative sign...
        if ((packet[5] & 0b00100000) == 0b00100000)
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

        if ((packet[14] & 0b10000000) == 0b10000000) // k
        {
            data.mainValue.unit.setPrefix(Data.Value.Unit.Prefix.KILO);
        }

        if ((packet[14] & 0b00001000) == 0b00001000) // M
        {
            data.mainValue.unit.setPrefix(Data.Value.Unit.Prefix.MEGA);
        }

        if ((packet[14] & 0b00000100) == 0b00000100) // u
        {
            data.mainValue.unit.setPrefix(Data.Value.Unit.Prefix.MICRO);
        }

        if ((packet[14] & 0b00000010) == 0b00000010) // m
        {
            data.mainValue.unit.setPrefix(Data.Value.Unit.Prefix.MILLI);
        }

        if ((packet[14] & 0b00000001) == 0b00000001) // n
        {
            data.mainValue.unit.setPrefix(Data.Value.Unit.Prefix.NANO);
        }

        // Main value unit measurement.
        data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.NONE);

        if ((packet[13] & 0b01000000) == 0b01000000) // Hz
        {
            data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.HERTZ);
        }

        if ((packet[13] & 0b00100000) == 0b00100000) // °F
        {
            data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.DEG_F);
        }

        if ((packet[13] & 0b00010000) == 0b00010000) // s
        {
            data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.S);
        }

        if ((packet[13] & 0b00000100) == 0b00000100) // ohm
        {
            data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.OHM);
        }

        if ((packet[13] & 0b00000010) == 0b00000010) // A
        {
            data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.AMPERE);
        }

        if ((packet[13] & 0b00000001) == 0b00000001) // F
        {
            data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.FARAD);
        }

        if ((packet[14] & 0b01000000) == 0b01000000) // V
        {
            data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.VOLT);
        }

        if ((packet[14] & 0b00100000) == 0b00100000) // S
        {
            // There are two units that use the same 'S' symbol on the display.
            // Determine the correct one by checking for pulse width (seconds).

            if ((packet[4] & 0b00010000) == 0b00010000) // PW
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

        if ((packet[14] & 0b00010000) == 0b00010000) // °C
        {
            data.mainValue.unit.setMeasurement(Data.Value.Unit.Measurement.DEG_C);
        }

        // Main value unit type.
        data.mainValue.unit.setType(Data.Value.Unit.Type.NONE); // Clear.

        if ((packet[5] & 0b01000000) == 0b01000000) // AC
        {
            data.mainValue.unit.setType(Data.Value.Unit.Type.AC);
        }

        if ((packet[5] & 0b00010000) == 0b00010000) // DC
        {
            data.mainValue.unit.setType(Data.Value.Unit.Type.DC);
        }

        if ((packet[4] & 0b00010000) == 0b00010000) // PW
        {
            data.mainValue.unit.setType(Data.Value.Unit.Type.PW);
        }

        // Sub digits...
        int digit9Bits = (((packet[2] << 4) | (packet[2] >> 4)) & 0b11111110);
        int digit8Bits = (((packet[1] << 4) | (packet[1] >> 4)) & 0b11111110);
        int digit7Bits = (((packet[0] << 4) | (packet[0] >> 4)) & 0b11111110);
        int digit6Bits = (((packet[19] << 4) | (packet[19] >> 4)) & 0b11111110);
        int digit5Bits = (((packet[18] << 4) | (packet[18] >> 4)) & 0b11111110);

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
        if ((packet[3] & 0b00000010) == 0b00000010)
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

        if ((packet[16] & 0b00000100) == 0b00000100) // m
        {
            data.subValue.unit.setPrefix(Data.Value.Unit.Prefix.MILLI);
        }

        if ((packet[16] & 0b00000010) == 0b00000010) // G
        {
            data.subValue.unit.setPrefix(Data.Value.Unit.Prefix.GIGA);
        }

        if ((packet[16] & 0b00000001) == 0b00000001) // M
        {
            data.subValue.unit.setPrefix(Data.Value.Unit.Prefix.MEGA);
        }

        if ((packet[17] & 0b00010000) == 0b00010000) // k
        {
            data.subValue.unit.setPrefix(Data.Value.Unit.Prefix.KILO);
        }

        // Sub value unit measurement.
        data.subValue.unit.setMeasurement(Data.Value.Unit.Measurement.NONE);

        if ((packet[16] & 0b00001000) == 0b00001000) // %
        {
            data.subValue.unit.setMeasurement(Data.Value.Unit.Measurement.PERCENT);
        }

        if ((packet[17] & 0b10000000) == 0b10000000) // dBm
        {
            data.subValue.unit.setMeasurement(Data.Value.Unit.Measurement.DECIBEL_MW);
        }

        if ((packet[17] & 0b01000000) == 0b01000000) // V
        {
            data.subValue.unit.setMeasurement(Data.Value.Unit.Measurement.VOLT);
        }

        if ((packet[17] & 0b00100000) == 0b00100000) // ohm
        {
            data.subValue.unit.setMeasurement(Data.Value.Unit.Measurement.OHM);
        }

        if ((packet[17] & 0b00001000) == 0b00001000) // °K
        {
            data.subValue.unit.setMeasurement(Data.Value.Unit.Measurement.KELVIN);
        }

        if ((packet[17] & 0b00000100) == 0b00000100) // A
        {
            data.subValue.unit.setMeasurement(Data.Value.Unit.Measurement.AMPERE);
        }

        if ((packet[17] & 0b00000010) == 0b00000010) // Hz
        {
            data.subValue.unit.setMeasurement(Data.Value.Unit.Measurement.HERTZ);
        }

        // Sub value unit type
        data.subValue.unit.setType(Data.Value.Unit.Type.NONE); // Clear.

        if ((packet[3] & 0b00000100) == 0b00000100) // AC
        {
            data.subValue.unit.setType(Data.Value.Unit.Type.AC);
        }

        if ((packet[3] & 0b00000001) == 0b00000001) // DC
        {
            data.subValue.unit.setType(Data.Value.Unit.Type.DC);
        }

        // Bar graph...
        Integer barGraph = null;

        if ((packet[4] & 0b10000000) == 0b10000000) // Bar graph '0' set.
        {
            // Bar graph displayed, so check all segments...
            barGraph = 0;
            barGraph += ((packet[4] & 0b01000000) >> 6); // B1.
            barGraph += ((packet[4] & 0b00001000) >> 2); // B2.
            barGraph += (packet[4] & 0b00000100); // B4.
            barGraph += ((packet[4] & 0b00000010) << 2); // B8.
            barGraph += ((packet[4] & 0b00000001) << 4); // B16.
            barGraph += ((packet[16] & 0b00010000) << 1); // B32.
            barGraph += ((packet[16] & 0b00100000) << 1); // B64.
            barGraph += ((packet[16] & 0b01000000) << 1); // B128.
            barGraph += ((packet[16] & 0b10000000) << 1); // B256.
            barGraph += ((packet[15] & 0b00001000) << 6); // B512.
            barGraph += ((packet[15] & 0b00000100) << 8); // B1K.
            barGraph += ((packet[15] & 0b00000010) << 10); // B2K.
            barGraph += ((packet[15] & 0b00000001) << 12); // B4K.
            barGraph += ((packet[15] & 0b00010000) << 9); // B8K.
            barGraph += ((packet[15] & 0b00100000) << 9); // B16K.
        }

        data.barGraph = barGraph;

        // Set flags...
        data.flags.autoOff = (packet[9] & 0b00100000) == 0b00100000;
        data.flags.pulse = (packet[9] & 0b10000000) == 0b10000000;
        data.flags.max = (packet[10] & 0b10000000) == 0b10000000;
        data.flags.posPeak = (packet[10] & 0b01000000) == 0b01000000;
        data.flags.rel = (packet[10] & 0b00100000) == 0b00100000;
        data.flags.recall = (packet[10] & 0b00010000) == 0b00010000;
        data.flags.goNg = (packet[10] & 0b00000001) == 0b00000001;
        data.flags.posPercent = (packet[10] & 0b00001000) == 0b00001000;
        data.flags.rs232 = (packet[9] & 0b00010000) == 0b00010000;
        data.flags.pos = (packet[8] & 0b00000100) == 0b00000100;
        data.flags.neg = (packet[8] & 0b00001000) == 0b00001000;
        data.flags.min = (packet[9] & 0b00001000) == 0b00001000;
        data.flags.negPeak = (packet[9] & 0b00000100) == 0b00000100;
        data.flags.avg = (packet[9] & 0b00000010) == 0b00000010;
        data.flags.store = (packet[9] & 0b00000001) == 0b00000001;
        data.flags.ref = (packet[10] & 0b00000010) == 0b00000010;
        data.flags.negPercent = (packet[10] & 0b00000100) == 0b00000100;
        data.flags.lowBattery = (packet[5] & 0b10000000) == 0b10000000;

        // The following are refered to as sub (value) units in the manual,
        // but they seem like global?
        data.flags.diode = (packet[3] & 0b10000000) == 0b10000000;
        data.flags.range = (packet[3] & 0b01000000) == 0b01000000;
        data.flags.hold = (packet[3] & 0b00100000) == 0b00100000;
        data.flags.duty = (packet[3] & 0b00010000) == 0b00010000;
        data.flags.audio = (packet[3] & 0b00001000) == 0b00001000;

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
     * @param encoded the value of the digit (8-bit value).
     *
     * @return A String representation of the digit value, either numerical or otherwise.
     */
    private String decodeDigit(int encoded)
    {
        // Define the masks to be checked.
        int mask9 = 0b11011110;
        int mask8 = 0b11111110;
        int mask7 = 0b10001010;
        int mask6 = 0b11110110;
        int mask5 = 0b11010110;
        int mask4 = 0b01001110;
        int mask3 = 0b10011110;
        int mask2 = 0b10111100;
        int mask1 = 0b00001010;
        int mask1P1 = 0b00001011;
        int mask0 = 0b11111010;
        int maskL = 0b01110000;
        int maskP = 0b11101100;
        int maskE = 0b11110100;
        int maskN = 0b00100110;
        int maskH = 0b01100110;
        int maskR = 0b00100100;
        int maskT = 0b01110100;
        int maskA = 0b01101110;
        int maskD = 0b00111110;
        int maskBlank = 0b00000000;

        // The order that the masks are checked is important! Rearanging this should be done with care.
        if ((encoded | maskBlank) == maskBlank)
        {
            return " ";
        }
        else if ((encoded & mask8) == mask8)
        {
            return "8";
        }
        else if ((encoded & maskA) == maskA)
        {
            return "A";
        }
        else if ((encoded & mask9) == mask9)
        {
            return "9";
        }
        else if ((encoded & mask6) == mask6)
        {
            return "6";
        }
        else if ((encoded & mask5) == mask5)
        {
            return "5";
        }
        else if ((encoded & mask4) == mask4)
        {
            return "4";
        }
        else if ((encoded & mask3) == mask3)
        {
            return "3";
        }
        else if ((encoded & mask2) == mask2)
        {
            return "2";
        }
        else if ((encoded & mask0) == mask0)
        {
            return "0";
        }
        else if ((encoded & maskD) == maskD)
        {
            return "d";
        }
        else if ((encoded & maskP) == maskP)
        {
            return "P";
        }
        else if ((encoded & maskE) == maskE)
        {
            return "E";
        }
        else if ((encoded & maskH) == maskH)
        {
            return "h";
        }
        else if ((encoded & maskN) == maskN)
        {
            return "n";
        }
        else if ((encoded & maskT) == maskT)
        {
            return "t";
        }
        else if ((encoded & maskR) == maskR)
        {
            return "r";
        }
        else if ((encoded & maskL) == maskL)
        {
            return "L";
        }
        else if ((( ~ encoded &  ~ mask1) ==  ~ mask1) || ( ~ encoded &  ~ mask1P1) ==  ~ mask1P1)
        {
            return "1";
        }
        else if ((encoded & mask7) == mask7)
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
