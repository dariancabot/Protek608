package com.dariancabot.protek608;

import java.util.Date;
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

    /**
     * Stores all of the readings data, both most recent and historical.
     */
    public Data data;

    private String[] portNames;

    private boolean isConnected;
    private Date lastConnect;


    public Protek608()
    {
        data = new Data();
        decoder = new Decoder(data);

        // Load existing serial ports.
        refreshSerialPorts();
    }


    private void refreshSerialPorts()
    {

        // First disconnect if needed...
        if (isConnected)
        {
            disconnectSerialPort();
        }

        // TODO: For more com port details (jssc can only give name), see: http://stackoverflow.com/q/6362775
        // Populate combobox with names of existing ports...
        System.out.println("Getting serial port list...");

        for (String port : jssc.SerialPortList.getPortNames())
        {
            // DO SOMETHING HERE (ADD TO LIST?)
            System.out.println("Found port: " + port);
        }

        portNames = jssc.SerialPortList.getPortNames();
    }


    public String[] getPortNames()
    {
        return portNames;
    }


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


    /**
     * Sets the SerialPort. If the current SerialPort is connected, it will attempt to close this first.
     *
     * @param serialPort The SerialPort to use.
     */
    private void setSerialPort(SerialPort serialPort)
    {
        disconnectSerialPort(); // Disconnect first.
        this.serialPort = serialPort;
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
            lastConnect = new Date();

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
