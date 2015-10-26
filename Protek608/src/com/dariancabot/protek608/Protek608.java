package com.dariancabot.protek608;

import jssc.SerialPort;
import jssc.SerialPortException;


/**
 *
 * @author Darian Cabot
 */
public final class Protek608
{
    private Communications communications;
    private SerialPort serialPort;
    private final Decoder decoder;

    private String[] portNames;
    private boolean isConnected;

    /**
     * Stores all of the readings data, both most recent and historical.
     */
    public Data data;


    /**
     * Defines the commands that can be sent to the Protek608 using the {@link #sendCommand(Command) sendCommand} method.
     */
    public enum Commands
    {
        HOLD(5),
        LIGHT(7),
        RANGE(6),
        MENU(8),
        ALT_F(1),
        LEFT(2),
        RIGHT(3),
        ENTER(4);

        private final int value;


        Commands(int value)
        {
            this.value = value;
        }


        /**
         * Gets the numerical value of the command.
         *
         * @return the numerical value of the command.
         */
        int getValue()
        {
            return value;
        }

    }


    public Protek608()
    {
        data = new Data();
        decoder = new Decoder(data);

        refreshSerialPorts(); // Load existing serial ports.
    }


    private void refreshSerialPorts()
    {

        // First disconnect if needed...
        if (isConnected)
        {
            disconnectSerialPort();
        }

        // For more com port details (jssc can only give name), see: http://stackoverflow.com/q/6362775
        portNames = jssc.SerialPortList.getPortNames();
    }


    public String[] getPortNames()
    {
        return portNames;
    }


    /**
     * Send a command to the Protek 608 multimeter over the serial connection.
     *
     * If not connected, there is no warning, the send command will fail silently.
     *
     * @param command
     */
    public void sendCommand(Commands command)
    {
        serialWrite((byte) command.getValue());
    }


    private void serialWrite(Byte data)
    {
        if (serialPort.isOpened())
        {
            try
            {
                serialPort.writeByte(data);
            }
            catch (SerialPortException e)
            {
                System.out.println("Failed to write.");
            }
        }
    }


    public boolean connectSerialPort(String port)
    {
        if ( ! isConnected)
        {
            if ( ! port.isEmpty())
            {
                serialPort = new SerialPort(port);
                initialiseSerialReader();

                return connectSerialPort();
            }
        }

        return false;
    }


    public void disconnectSerialPort()
    {
        if (isConnected)
        {
            try
            {
                serialPort.closePort();
                isConnected = false;
                data.mainValue.statistics.isEnabled(false);
            }
            catch (SerialPortException spe)
            {
                System.err.println("Error closing Serial Port: " + spe.getMessage());
            }
        }
    }


    private void initialiseSerialReader()
    {
        if (serialPort == null)
        {
            System.err.println("SerialPort must be set before SerialPortReader is initialised!");
            return;
        }

        communications = new Communications(serialPort, decoder);
    }


    private boolean connectSerialPort()
    {
        try
        {
            serialPort.openPort(); // Open port
            serialPort.setParams(9600, 7, 1, 0); // Set params
            int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_CTS + SerialPort.MASK_DSR; // Prepare mask
            serialPort.setEventsMask(mask); // Set mask
            //serialPort.addEventListener(new Communications()); // Add SerialPortEventListener
            serialPort.addEventListener(communications); // Add SerialPortEventListener

            System.out.println("Connected to serial port: " + serialPort.getPortName() + ".");

            isConnected = true;

            data.mainValue.statistics.isEnabled(true);

            return true;
        }
        catch (SerialPortException ex)
        {
            System.err.println(ex);

            return false;
        }
    }


    public void setEventListener(EventListener eventListener)
    {
        this.decoder.setEventListener(eventListener);
    }

}
