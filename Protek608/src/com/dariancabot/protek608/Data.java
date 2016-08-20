/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Darian Cabot
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.dariancabot.protek608;

import java.util.ArrayList;
import java.util.Date;


/**
 * Data Object.
 *
 * @author Darian Cabot
 */
public final class Data
{

    /**
     * The value of the main (large) reading on the DMM.
     */
    public Value mainValue = new Value();

    /**
     * The value of the sub (small) reading on the DMM.
     */
    public Value subValue = new Value();

    /**
     * Numerical value of the bar graph.
     * <p>
     * Range: 0 to 32767 (i.e.: 0111 1111 1111 1111).
     * <p>
     * Note: A null value means the bar graph is not being displayed on the DMM.
     */
    public Integer barGraph = null;

    /**
     * The packet data direct from the communications buffer.
     */
    public byte[] packetRaw = null;

    /**
     * The packet data without the start/end bytes.
     */
    public byte[] packetTidy = null;


    /**
     * Value representation of the DMM.
     * <p>
     * This includes the value, measurement unit, and statistics.
     */
    public static class Value
    {

        private String value;
        private String valueVerbatim;

        /**
         * The measurement unit.
         */
        public Unit unit = new Unit();

        /**
         * Statistics for this value like minimum, maximum, average.
         */
        public Statistics statistics = new Statistics();


        /**
         * The measurement unit.
         */
        public static class Unit
        {
            private Type type = Type.NONE;
            private Prefix prefix = Prefix.NONE;
            private Measurement measurement = Measurement.NONE;


            /**
             * The signal type.
             * <p>
             * Types that can be used:
             * <ul>
             * <li>{@link #AC} - Alternating Current
             * <li>{@link #DC} - Direct Current
             * <li>{@link #PW} - Pulse Width
             * </ul>
             */
            public enum Type
            {
                /**
                 * No signal type defined.
                 */
                NONE(null, null),
                /**
                 * Alternating current signal type.
                 */
                AC("AC", "Alternating Current"),
                /**
                 * Direct current signal type.
                 */
                DC("DC", "Direct Current"),
                /**
                 * Pulse width signal type.
                 */
                PW("PW", "Pulse Width");

                private final String abbreviation;
                private final String name;

                Type(String abbreviation, String name)
                {
                    this.abbreviation = abbreviation;
                    this.name = name;
                }

                /**
                 * Gets the abbreviation of the signal type.
                 *
                 * @return the abbreviation of the signal type.
                 */
                public String getAbbreviation()
                {
                    return this.abbreviation;
                }

                /**
                 * Gets the full name of the signal type.
                 *
                 * @return the full nameof the signal type.
                 */
                public String getName()
                {
                    return this.name;
                }

            }


            /**
             * The unit SI prefix (scale multiplier) for the measurement unit.
             * <p>
             * Prefixes that can be used:
             * <ul>
             * <li>{@link #NONE}
             * <li>{@link #NANO}
             * <li>{@link #MICRO}
             * <li>{@link #MILLI}
             * <li>{@link #KILO}
             * <li>{@link #MEGA}
             * <li>{@link #GIGA}
             * </ul>
             */
            public enum Prefix
            {
                /**
                 * No unit prefix (whole unit).
                 */
                NONE(null, null, 0),
                /**
                 * Nano unit prefix.
                 * <p>
                 * For example:<br>
                 * <ul>
                 * <li>Billionths of the unit</li>
                 * <li>Unit x 10<sup>-9</sup></li>
                 * <li>Unit x 0.000000001</li>
                 * </ul>
                 */
                NANO("n", "Nano", -9),
                /**
                 * Micro unit prefix.
                 * <p>
                 * For example:<br>
                 * <ul>
                 * <li>Millionths of the unit</li>
                 * <li>Unit x 10<sup>-6</sup></li>
                 * <li>Unit x 0.000001</li>
                 * </ul>
                 */
                MICRO("µ", "Micro", -6),
                /**
                 * Milli unit prefix.
                 * <p>
                 * For example:<br>
                 * <ul>
                 * <li>Thousandths of the unit</li>
                 * <li>Unit x 10<sup>-3</sup></li>
                 * <li>Unit x 0.001</li>
                 * </ul>
                 */
                MILLI("m", "Milli", -3),
                /**
                 * Kilo unit prefix.
                 * <p>
                 * For example:<br>
                 * <ul>
                 * <li>Thousands of the unit</li>
                 * <li>Unit x 10<sup>3</sup></li>
                 * <li>Unit x 1000</li>
                 * </ul>
                 */
                KILO("k", "Kilo", 3),
                /**
                 * Mega unit prefix.
                 * <p>
                 * For example:<br>
                 * <ul>
                 * <li>Millions of the unit</li>
                 * <li>Unit x 10<sup>6</sup></li>
                 * <li>Unit x 1,000,000</li>
                 * </ul>
                 */
                MEGA("M", "Mega", 6),
                /**
                 * Giga unit prefix.
                 * <p>
                 * For example:<br>
                 * <ul>
                 * <li>Billions of the unit</li>
                 * <li>Unit x 10<sup>9</sup></li>
                 * <li>Unit x 1,000,000,000</li>
                 * </ul>
                 */
                GIGA("G", "Giga", 9);

                private final String abbreviation;
                private final String name;
                private final int exponent; // Value x 10^?

                Prefix(String abbreviation, String name, int exponent)
                {
                    this.abbreviation = abbreviation;
                    this.name = name;
                    this.exponent = exponent;
                }

                /**
                 * Gets the abbreviation of the unit prefix.
                 *
                 * @return the unit prefix abbreviation.
                 */
                public String getAbbreviation()
                {
                    return this.abbreviation;
                }

                /**
                 * Gets the name of the unit prefix.
                 *
                 * @return the unit prefix name.
                 */
                public String getName()
                {
                    return this.name;
                }

                /**
                 * Gets the exponent for power of 10 (useful for math scaling).
                 * <p>
                 * Example: 10<sup>exponent</sup>
                 *
                 * @return the unit prefix's exponent.
                 */
                public int getExponent()
                {
                    return this.exponent;
                }

            }


            /**
             * The unit measurement.
             * <p>
             * Measurements that can be used:
             * <ul>
             * <li>{@link #NONE}
             * <li>{@link #VOLT}
             * <li>{@link #AMPERE}
             * <li>{@link #OHM}
             * <li>{@link #FARAD}
             * <li>{@link #HERTZ}
             * <li>{@link #PERCENT}
             * <li>{@link #DEG_C}
             * <li>{@link #DEG_F}
             * <li>{@link #KELVIN}
             * <li>{@link #DECIBEL_MW}
             * <li>{@link #SIEMENS}
             * <li>{@link #SECOND}
             * <li>{@link #S}
             * </ul>
             */
            public enum Measurement
            {
                NONE(null, null),
                VOLT("V", "Volt"),
                AMPERE("A", "Ampere"),
                OHM("Ω", "Ohm"),
                FARAD("F", "Farad"),
                HERTZ("Hz", "Hertz"),
                PERCENT("%", "Percent"),
                DEG_C("°C", "Degrees Celcius"),
                DEG_F("°F", "Degrees Fahrenheit"),
                KELVIN("K", "Kelvin"),
                DECIBEL_MW("dBm", "Decibel-milliwatt"),
                SIEMENS("S", "Siemens"),
                SECOND("S", "Second"),
                S("s", "s"); // TODO: What is this unit? (Ref 108 from manual page 44).

                private final String abbreviation;
                private final String name;

                Measurement(String abbreviation, String name)
                {
                    this.abbreviation = abbreviation;
                    this.name = name;
                }

                /**
                 * Gets the abbreviation of the measurement unit.
                 *
                 * @return the measurement unit abbreviation.
                 */
                public String getAbbreviation()
                {
                    return this.abbreviation;
                }

                /**
                 * Gets the name of the measurement unit.
                 *
                 * @return the measurement unit name.
                 */
                public String getName()
                {
                    return this.name;
                }

            }

            /**
             * Gets the signal type.
             *
             * @return the signal type.
             */
            public Type getType()
            {
                return type;
            }

            /**
             * Sets the signal type.
             *
             * @param type the signal type.
             */
            public void setType(Type type)
            {
                this.type = type;
            }

            /**
             * Gets the measurement unit prefix.
             *
             * @return the measurement unit prefix.
             */
            public Prefix getPrefix()
            {
                return prefix;
            }

            /**
             * Sets the measurement unit prefix.
             *
             * @param prefix the measurement unit prefix.
             */
            public void setPrefix(Prefix prefix)
            {
                this.prefix = prefix;
            }

            /**
             * Gets the measurement type.
             *
             * @return the measurment type.
             */
            public Measurement getMeasurement()
            {
                return measurement;
            }

            /**
             * Sets the measurement type.
             *
             * @param measurement the measurement type.
             */
            public void setMeasurement(Measurement measurement)
            {
                this.measurement = measurement;
            }

            /**
             * Gets a String representation of the unit in a concise, readable format.
             *
             * <p>
             * Format: [prefix][measurement] [type]
             *
             * @return A representaiton of the unit.
             */
            @Override
            public String toString()
            {
                String unit = "";

                if (this.prefix.getAbbreviation() != null)
                {
                    unit += this.prefix.getAbbreviation();
                }

                if (this.measurement.getAbbreviation() != null)
                {
                    unit += this.measurement.getAbbreviation();
                }

                if (this.type.getAbbreviation() != null)
                {
                    unit += " " + this.type.getAbbreviation();
                }

                unit = unit.trim();

                if (unit.isEmpty())
                {
                    unit = null;
                }

                return unit;
            }

        }


        public static class Statistics
        {

            private boolean isEnabled;

            private long samples;
            private Date durationStart = new Date();
            private long duration;
            private Double minimum;
            private Double maximum;
            private final ArrayList<Double> averageValues = new ArrayList<>();
            private Double average;

            /**
             * Enable or disable the accumulation of statistical data.
             * <p>
             * Disabling does not reset statistics data. To also clear data, call the {@link reset()} function.
             *
             * @param isEnabled true to enable, false to disable.
             */
            public void setEnabled(boolean isEnabled)
            {
                this.isEnabled = isEnabled;
            }

            /**
             * Gets the enabled status of the statistics.
             *
             * @return true if the statistics are enabled.
             */
            public boolean isEnabled()
            {
                return isEnabled;
            }

            /**
             * Resets all statistics by clearing all counters, averages, and other values. Dues not change the "enabled" parameter.
             */
            public void reset()
            {
                samples = 0;

                durationStart = new Date();
                duration = 0;

                minimum = null;
                maximum = null;
                average = null;
                averageValues.clear();
            }

            /**
             * Updates the statistics with a new value.
             *
             * @param value the reading/measurement value.
             */
            protected void update(double value)
            {
                if ( ! isEnabled)
                {
                    return;
                }

                if (samples < 1)
                {
                    // Statistics need to be initialised...
                    samples = 1;
                    durationStart = new Date();
                    duration = 500; // Start at half a second (refresh rate is 2Hz).
                    minimum = value;
                    maximum = value;
                    average = value;
                    averageValues.clear();
                    averageValues.add(value);
                }
                else
                {
                    samples += 1L;

                    Date now = new Date();
                    duration = (now.getTime() - durationStart.getTime()) / 1000L;

                    minimum = Math.min(minimum, value);
                    maximum = Math.max(maximum, value);

                    averageValues.add(value);
                    Double valueSum = 0d;

                    for (Double avgValue : averageValues)
                    {
                        valueSum += avgValue;
                    }

                    average = valueSum / averageValues.size();
                }
            }

            /**
             * Gets the number of samples used to calculate statistics.
             *
             * @return the number of samples.
             */
            public long getSamples()
            {
                return samples;
            }

            /**
             * Gets the date-time of when the statistics began (i.e. date-time of the first sample).
             *
             * @return the date-time when statistics began.
             */
            public Date getDurationStart()
            {
                return durationStart;
            }

            /**
             * Gets the duration of statistics gathering in seconds.
             *
             * @return the length of time in seconds.
             */
            public Long getDuration()
            {
                return duration;
            }

            /**
             * Gets the minimum value of all samples.
             *
             * @return the minimum value.
             */
            public Double getMinimum()
            {
                return minimum;
            }

            /**
             * Gets the maximum value of all samples.
             *
             * @return the maximum value.
             */
            public Double getMaximum()
            {
                return maximum;
            }

            /**
             * Gets the average of all samples.
             *
             * @return the average of all samples.
             */
            public Double getAverage()
            {
                return average;
            }

        }

        /**
         * The value as a string not including the measurement unit.
         * <p>
         * To include the measurement unit use the {@link #toString(boolean)} function.
         *
         * @return a String representation of the value.
         */
        @Override
        public String toString()
        {
            return toString(false);
        }

        /**
         * The value as a string not including the measurement unit.
         *
         * @param includeUnit true to include the measurement unit.
         *
         * @return a String representation of the value.
         */
        public String toString(boolean includeUnit)
        {
            String valueStr = value;

            if ((includeUnit) && (unit.toString() != null))
            {
                valueStr += " " + unit.toString();
            }

            return valueStr;
        }

        /**
         * Sets the value as a String.
         * <p>
         * If numerical it will also used in the statistics if enabled.
         *
         * @param value String of the value.
         */
        public void setValue(String value)
        {
            // Update the value.
            this.valueVerbatim = value;
            this.value = value.trim();

            // Update statistics if value is numeric...
            if (isNumeric(this.value))
            {
                statistics.update(Double.parseDouble(this.value));
            }
        }

        /**
         * Gets the value represented as a String that resembles what is displayed on the LCD.
         *
         * This also includes non-numerical values (i.e. words on LCD like "Shrt", "OPEn").
         * <p>
         * For an exact representation (including whitespace), use the {@link #getValueVerbatim() getValueVerbatim} method.
         * <p>
         * For a numerical value, {@link #getValueDouble() getValueDouble} use the method.
         *
         * @return
         */
        public String getValue()
        {
            return value;
        }

        /**
         * Gets a more accurate representation what the value looks like on the Protek608 LCD including whitespace padding, etc.
         *
         * This also includes non-numerical values (i.e. words on LCD like "Shrt", "OPEn").
         *
         * @return String value that accurately representd Protek LCD.
         */
        public String getValueVerbatim()
        {
            return valueVerbatim;
        }

        /**
         * Gets the value as a Double if numerical, otherwise returns null.
         *
         * @return a Double value if numerical, or null if not-numerical (i.e. words on LCD like "Shrt", "OPEn").
         */
        public Double getValueDouble()
        {
            if (isNumeric(value))
            {
                return Double.parseDouble(value);
            }
            else
            {
                return null;
            }
        }

        /**
         * Match a number with optional '-' and decimal.
         *
         * Note: Will fail if non-latin (i.e. 0 to 9) digits used (for example, arabic digits).
         *
         * @see http://stackoverflow.com/a/1102916
         *
         * @param string The String to check.
         *
         * @return boolean True if numeric, false if not.
         */
        private boolean isNumeric(String string)
        {
            return string.matches("-?\\d+(\\.\\d+)?");
        }

    }

    public Annunciators annunciators = new Annunciators();


    /**
     * Annunciators are modes or options that the Protek 608 DMM can have enabled or disabled.
     *
     * These are usually displayed on the LCD, and the status of each is available as a boolean in this class.
     */
    public class Annunciators
    {
        /**
         * This Feature is used for conserving battery power. When the meter is left on for more than 15 minutes without pressing keys or rotating the
         * rotary switch the meter will shut off (go in to Auto power off status).
         * <p>
         * The meter can be restarted by pressing the ENTER key or turning the rotary switch to the off position and then back to its original
         * position.
         * <p>
         * This feature can be disabled by selecting the Auto off annunciator from the menu and then press the enter Key. The Annunciator will
         * disappear from the LCD. This is the continuous use mode; the meter will operate until the battery is drained.
         */
        public boolean autoOff;
        /**
         * Indicates the polarity of the pulse being measured in pulse width and duty cycle function.
         */
        public boolean pulse; // TODO: Should be coupled with pos/neg. See manual page 35.
        /**
         * The MAX capture mode stores the highest of the measured values into memory and displays this on the main display.
         * <p>
         * The meter can capture and hold signal level changes 100mS or greater in duration. If a shorter capture time is required, use the peak
         * detection mode.
         * <p>
         * To display the MAX value:
         * <ol>
         * <li>Press the MENU key
         * <li>Press the RIGHT or LEFT key to move the blinking cursor to MAX
         * <li>Press the ENTER key to select and start MAX capture. To clear the present value of MAX or restart the capture mode, press the ENTER
         * key.
         * </ol>
         */
        public boolean maximum;
        /**
         * The peak detection mode is for capturing high-speed changes in signal level of 5mS or greater in duration. This mode is available only in
         * DCMV, DCV, DCUA, DCMA and DCA.
         * <p>
         * To use this mode press the MENU key and move the blinking cursor to + Peak for positive signals or - Peak ({@link #negPeak}) for negative
         * signals with the RIGHT or LEFT keys then press ENTER. To clear the present value of the peak or to restart the peak detection mode, press
         * the ENTER key only.
         */
        public boolean posPeak;
        /**
         * Relative mode.
         */
        public boolean relative;
        /**
         * This function recalls data from a memory location that data has been previously stored ({@link #store}).
         * <p>
         * To Recall a memory location:
         * <ol>
         * <li>Press the MENU key.
         * <li>Move the LEFT or RIGHT key to position the blinking cursor over recall.
         * <li>Press the ENTER key.
         * <li>Press the LEFT or RIGHT key to select the desired memory address. The main display will read the contents of the memory indicated by
         * the address number on the secondary display.
         * </ol>
         */
        public boolean recall;
        /**
         * The GO/NG function provides an easy way to determine if a reading falls within a designated range of values. The LCD indicates on the
         * primary display if the input value is out of (fail) or within the range (pass), which you selected. Before starting the GO/NG function, the
         * tolerance range that an input value will be compared against must be set. This can be accomplished with the following procedure:
         * <ol>
         * <li>Press the menu key.
         * <li>Move the blinking cursor to GO/NG.
         * <li>Press the ENTER key to select the GO/NG function mode and to enter the input reference value. The display will show the memory address
         * number in the secondary display and the memory contents in the main display used as the reference value.
         * <li>There are 2 ways for inputting the reference value. Method 1: Direct input of reference value by the following method. Press the ENTER
         * key for longer than 1sec to modify the contents of the reference memory. The first digit will start blinking. The digit value may be
         * changed by pressing the LEFT/RIGHT arrow keys. Pressing the ENTER key again will cause the second digit to blink. Press the LEFT/RIGHT keys
         * to change the digit value as required and then press the enter key. This will make the next digit blink. In the same manner, change its
         * value and likewise for all the remaining digits. After the last digit is entered, the negative sign will blink. Press the LEFT/RIGHT keys
         * to select positive or negative and press the enter key. This will make the first decimal point blink. Press the LEFT/RIGHT keys to move the
         * dedmal point to appropriate position and then press the enter key. This oompletes the input of reference value and displays the +%
         * annunciator on the top right of the display and a blinking secondary display. The +% tolerance is entered through the secondary display by
         * pressing the RIGHT/LEFT keys Pressing the ENTER key will store the + tolerance value and display the Tolerance annunciator To enter the %
         * value use the same procedure as for the +% tolerance value. C1nce the -% tolerance is entered the GO/NG function will start.
         * <li>The second method is the indirect input from one of 10 memories (see {@link #store} and {@link #recall}). The data stored in one of the
         * 10 memory locations can be used as the reference by the following procedure: fuform the-Steps 1 through 3 in this section. Press the
         * LefVRight keys to select the memory address number where the reference value is stored, then press the ENTER key. This setS the input
         * reference value. To set the +% and % tolerances use the procedure in step 4 above.
         * </ol>
         */
        public boolean goNg;
        public boolean posPercent;
        /**
         * Serial data interface with computer
         */
        public boolean rs232c;
        public boolean positive; // TODO: Should be used in conjunction with PULSE.
        public boolean negative; // TODO: Should be used in conjunction with PULSE.
        /**
         * The MIN capture mode stores the lowest of the measured values into memory and displays this on the main display.
         * <p>
         * The meter can capture and hold signal level changes 100mS or greater in duration. If a shorter capture time is required, use the peak
         * detection mode.
         * <p>
         * To display the MIN value:
         * <ol>
         * <li>Press the MENU key
         * <li>Press the RIGHT or LEFT key to move the blinking cursor to MIN
         * <li>Press the ENTER key to select and start MIN capture. To clear the present value of MIN or restart the capture mode, press the ENTER
         * key.
         * </ol>
         */
        public boolean minimum;
        /**
         * The peak detection mode is for capturing high-speed changes in signal level of 5mS or greater in duration. This mode is available only in
         * DCMV, DCV, DCUA, DCMA and DCA.
         * <p>
         * To use this mode press the MENU key and move the blinking cursor to + Peak ({@link #posPeak}) for positive signals or - Peak for negative
         * signals with the RIGHT or LEFT keys then press ENTER. To clear the present value of the peak or to restart the peak detection mode, press
         * the ENTER key only.
         */
        public boolean negPeak;
        /**
         * The AVG function mode is useful for measuring a signal, which contains ripple, noise, or fluctuations.
         * <p>
         * Strictly speaking; this AVG function is different from the average by mathematical definition. More exactly it is a smoothing function,
         * which reduces changes due to ripple or fluctuation by 100 times from the following calculation:
         * <p>
         * AVG = [sum of previous 100 data measured] / 100
         */
        public boolean average;
        /**
         * Up to 10 measurements can be stored or recalled ({@link #recall}) in memory at any time.
         * <p>
         * To store a measurement value in memory:
         * <ol>
         * <li>Press the MENU key
         * <li>Press the LEFT/RIGHT arrow keys to position the blinking cursor over the STORE annunciator
         * <li>Press the ENTER key to select the store mode.
         * <li>Press the left or right key to select appropriate memory address. The secondary display will show the address number. Pressing the
         * ENTER key stores the displayed measurement reading Note Only numerical data can be stored in memory. OPEN/SHRT Continuity and GO/NG test
         * data cannot be stored in memory
         * <li>The memories used for this function are nonvolatile type of EEPROM. So the memory contents are not erased when the batteries go dead or
         * replace. The only way to change the data is by writing new data to a location.
         * </ol>
         */
        public boolean store;
        /**
         * Reference value for going test
         */
        public boolean reference;
        public boolean negPercent;
        /**
         * A Battery symbol appears on the display when the battery voltage falls below 6±1V.
         * <p>
         * The Battery symbol warns the user to replace the current battery with a new one.
         */
        public boolean lowBattery;
        /**
         * Indicates manual range mode
         */
        public boolean range;
        /**
         * Indicates the data hold key was pressed.
         */
        public boolean hold;
        /**
         * Duty cycle test
         */
        public boolean duty;
        /**
         * Continuity test mode
         */
        public boolean continuity;
        /**
         * Zener diode test
         */
        public boolean zenerDiode;
    }

}
