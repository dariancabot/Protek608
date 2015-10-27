package com.dariancabot.protek608;

import jssc.SerialPort;
import jssc.SerialPortException;


/**
 * Protek 608 Object.
 *
 * <p>
 * Models the attributes, commands, and measurement of the Protek 608 digital multimeter (DMM).
 * <p>
 * Connection is established using the DMM's RS-232 port and a host system Serial Port using the jSSC library.
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
     * Stores all of the readings data, both most recent and statistical.
     */
    public Data data;


    //-----------------------------------------------------------------------
    /**
     * Defines the commands that can be sent to the Protek608 using the {@link #sendCommand(Command) sendCommand} method.
     *
     * <p>
     * Commands that can be used:
     * <ul>
     * <li>{@link #HOLD}
     * <li>{@link #LIGHT}
     * <li>{@link #RELATIVE}
     * <li>{@link #MENU}
     * <li>{@link #ALT_FUNCTION}
     * <li>{@link #LEFT}
     * <li>{@link #RIGHT}
     * <li>{@link #ENTER}
     * </ul>
     */
    public enum Commands
    {
        /**
         * The HOLD command 'freezes' the measurement reading on the LCD. No further updates to the LCD display will happen and the HOLD annunciator is
         * displayed. Sending the HOLD command again restores the DMM to normal operation.
         */
        HOLD(5),
        /**
         * The LIGHT command turns on the LCD back light; sending the command again will turn it off. In order to conserve battery power the backlight will
         * automatically shut off 30 seconds after it is turned on.
         */
        LIGHT(7),
        /**
         * The relative mode allows the operator to measure values with respect to a reference value other than zero. The relative value is computed by the
         * equation: Relative = measured - reference.
         * <p>
         * Sending the RELATIVE command enters the measured value on the LCD as the reference and displays the REL annunciator. Sending the ENTER command only
         * while in the REL mode updates the Reference Value.
         * <p>
         * Send the REL command to enter the measured value on the LCD as the relative value and to display the REL symbol. Sending the REL command again
         * releases the relative mode and returns the DMM to the normal mode of operation.
         * <p>
         * Note:
         * <ul>
         * <li>The REL mode can only be used for numerical data; it cannot be used for continueinuity, which displays 'open' or 'short' instead of numbers.
         * <li>The REL mode is especially useful for low ohms measurement, which requires the test lead resistance to be cancelled.
         * </ul>
         */
        RELATIVE(6),
        /**
         * Sending the MENU command places the meter in the menu mode. Sending this command again will exit from the menu mode and return to the previous
         * operation.
         * <p>
         * Once the meter is in the menu mode, all the menu annunciators appear on the upper portion of the LCD with the flashing cursor over one annunciator.
         * To select the desired menu item, send the {@link #LEFT} or {@link #RIGHT} commands until the flashing cursor is over the desired annunciator, then
         * send the {@link #ENTER} command to select.
         * <p>
         * The exception to this procedure is the GO/NO Testing function, which is explained in detail in chapter 5 sections 5-7 of the Protek 608 manual.
         */
        MENU(8),
        /**
         * The ALT_FUNCTION command is used for selecting the alternate functions, which share the same position on the rotary function switch (e.g. Hz/PW).
         * <p>
         * When the Function selector switch is rotated to this position the default function is HZ. Sending the ALT_FUNCTION command will select PW (pulse
         * width).
         */
        ALT_FUNCTION(1),
        /**
         * The LEFT command is used to select the manual range mode and shift the present measurement range one decimal place to the left. Each time this key is
         * pressed the decimal point will shift one place to the left.
         * <p>
         * Sending this command in the Menu mode will cause the blinking cursor to move to the left.
         */
        LEFT(2),
        /**
         * The RIGHT command is used to select the manual range mode and shift the present measurement range one decimal place to the right. Each time this key
         * is pressed the decimal point will shift one place to the right.
         * <p>
         * Sending this command in the Menu mode will cause the blinking cursor to move to the right.
         */
        RIGHT(3),
        /**
         * Sending the ENTER command executes the function selected by the {@link #RIGHT} and {@link #LEFT} commands when used with the {@link #MENU} command.
         * When sent, the blinking annunciator will stop blinking and all the other menu items will disappear. If however, the "AUTO OFF" and "RS232C"
         * annunciators had been selected previously they will remain on the LCD.
         * <p>
         * Another important function of this key is to restore the power to the meter after AUTO POWER OFF has occurred.
         */
        ENTER(4);

        private final int value;


        private Commands(int value)
        {
            this.value = value;
        }


        //-----------------------------------------------------------------------
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


    //-----------------------------------------------------------------------
    /**
     * Constructor.
     *
     */
    public Protek608()
    {
        data = new Data();
        decoder = new Decoder(data);
    }


    //-----------------------------------------------------------------------
    /**
     * Gets the found available Serial Port on the host system.
     *
     * @return a String array of availble Serial Ports, empty array if none found.
     */
    public String[] getPortNames()
    {
        // For more com port details (jssc can only give name), see: http://stackoverflow.com/q/6362775
        portNames = jssc.SerialPortList.getPortNames();

        return portNames;
    }


    //-----------------------------------------------------------------------
    /**
     * Sends a command to the Protek 608 multimeter over the serial connection.
     *
     * If not connected, there is no warning, the send command will fail silently.
     *
     * @param command The {@link Commands} value to be sent to the DMM
     */
    public void sendCommand(Commands command)
    {
        serialWrite((byte) command.getValue());
    }


    //-----------------------------------------------------------------------
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


    //-----------------------------------------------------------------------
    /**
     * Connects/opens the Serial Port connection.
     *
     * @param port The String reprentation of the Serial Port (i.e. "COM3")
     *
     * @return true if connection successful, otherwise false.
     */
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


    //-----------------------------------------------------------------------
    /**
     * Disconnects/closes the Serial Port connection.
     */
    public void disconnectSerialPort()
    {
        if (isConnected)
        {
            try
            {
                serialPort.closePort();
                isConnected = false;
                data.mainValue.statistics.setEnabled(false);
            }
            catch (SerialPortException spe)
            {
                System.err.println("Error closing Serial Port: " + spe.getMessage());
            }
        }
    }


    //-----------------------------------------------------------------------
    private void initialiseSerialReader()
    {
        if (serialPort == null)
        {
            System.err.println("SerialPort must be set before SerialPortReader is initialised!");
            return;
        }

        communications = new Communications(serialPort, decoder);
    }


    //-----------------------------------------------------------------------
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

            data.mainValue.statistics.setEnabled(true);

            return true;
        }
        catch (SerialPortException ex)
        {
            System.err.println(ex);

            return false;
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
        this.decoder.setEventListener(eventListener);
    }

}
