package com.dariancabot.protek608;

import com.dariancabot.protek608.exceptions.ProtocolException;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;


/**
 *
 * @author Darian Cabot
 */
public final class Communications implements SerialPortEventListener
{
    private SerialPort serialPort;
    private final Decoder decoder;

    private final byte packetStartByte = 0x5b;
    private final byte packetEndByte = 0x5d;

    // RS-232 Control lines.
    // TODO: Irrelevant? Remove?
    private boolean isCtsOn = false; // CTS = Clear To Send.
    private boolean isDsrOn = false; // DSR = Data Set Ready.

    private final byte[] packetBuffer = new byte[43];
    private int packetBufferPosition = 0;
    private boolean packetBufferActive = false;


    //-----------------------------------------------------------------------
    /**
     * Creates a new Communications instance.
     *
     * @param serialPort the SerialPort to be used
     * @param decoder    the Decoder to be used
     */
    public Communications(SerialPort serialPort, Decoder decoder)
    {
        this.serialPort = serialPort;
        this.decoder = decoder;
    }


    //-----------------------------------------------------------------------
    /**
     * Gets the SerialPort used for communications.
     *
     * @return the SerialPort used for communications
     */
    protected SerialPort getSerialPort()
    {
        return serialPort;
    }


    //-----------------------------------------------------------------------
    /**
     * Sets the SerialPort to be used for communications.
     *
     * @param serialPort the new SerialPort
     */
    protected void setSerialPort(SerialPort serialPort)
    {
        this.serialPort = serialPort;
    }


    //-----------------------------------------------------------------------
    /**
     * Implementation of the serialEvent method to see events that happened to the port. This only report those events that are set in the SerialPort
     * mask.
     *
     * @param event the new SerialPort
     */
    @Override
    public void serialEvent(SerialPortEvent event)
    {

        switch (event.getEventType())
        {
            case SerialPortEvent.RXCHAR: // Data has been received.

                try
                {
                    byte[] rxBuffer = serialPort.readBytes();

                    for (int byteCount = 0; byteCount < rxBuffer.length; byteCount ++)
                    {
                        packetBuffer[packetBufferPosition] = rxBuffer[byteCount];

                        // Buffer overflow protection.
                        if (packetBufferPosition >= 42)
                        {
                            // Reset for next packet
                            packetBufferActive = false;
                            packetBufferPosition = 0;
                        }
                        else if (packetBufferActive)
                        {
                            packetBufferPosition ++;

                            if ((packetBufferPosition == 42) && (packetBuffer[packetBufferPosition] == packetEndByte))
                            {
                                // We have a full valid packet, decode it.
                                decoder.decodeSerialData(packetBuffer);
                            }
                        }
                        else if (packetBuffer[0] == 0x5b)//packetStartByte)
                        {
                            packetBufferActive = true;
                            packetBufferPosition = 1;
                        }
                    }
                }
                catch (SerialPortException | ProtocolException e)
                {
                    ProtocolException pex = new ProtocolException("Error receiving serial data", e);
                    throw pex;
                }

                break;

            case SerialPortEvent.CTS:

                isCtsOn = (event.getEventValue() == 1); // If signal line is ON

                break;

            case SerialPortEvent.DSR:

                isDsrOn = (event.getEventValue() == 1); // If signal line is ON

                break;

            default:
                break;
        }
    }


    //-----------------------------------------------------------------------
    /**
     * Gets the status of the RS-232 CTS (Clear To Send) control line.
     *
     * @return the status of the CTS control line
     */
    public boolean isCtsOn()
    {
        return isCtsOn;
    }


    //-----------------------------------------------------------------------
    /**
     * Gets the status of the RS-232 DSR (Data Set Ready) control line.
     *
     * @return the status of the DSR control line
     */
    public boolean isDsrOn()
    {
        return isDsrOn;
    }

}
