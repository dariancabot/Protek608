package com.dariancabot.protek608;

import java.util.ArrayList;
import java.util.Date;


/**
 *
 * @author Darian Cabot
 */
public final class Data
{

    public Value mainValue = new Value();
    public Value subValue = new Value();

    public Date lastUpdate = new Date();

    public int barGraph = 0;


    public class Value
    {

        private String value;
        private String valueVerbatim;

        private String unit; // TODO: All main/sub units as struct thingy.

        public Statistics statistics = new Statistics();


        public class Statistics
        {

            private boolean isEnabled;

            private long samples;
            private Date durationStart = new Date();
            private long duration;
            private Double minimum;
            private Double maximum;
            private final ArrayList<Double> averageValues = new ArrayList<>();
            private Double average;


            public void isEnabled(boolean value)
            {
                isEnabled = value;
            }


            public boolean isEnabled()
            {
                return isEnabled;
            }


            /**
             * Resets all statistics by clearing all counters, averages, and
             * other values. Dues not change the "enabled" parameter.
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


            private void update(double value)
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


            public boolean isIsEnabled()
            {
                return isEnabled;
            }


            public long getSamples()
            {
                return samples;
            }


            public Date getDurationStart()
            {
                return durationStart;
            }


            public Long getDuration()
            {
                return duration;
            }


            public Double getMinimum()
            {
                return minimum;
            }


            public Double getMaximum()
            {
                return maximum;
            }


            public Double getAverage()
            {
                return average;
            }


            public void setIsEnabled(boolean isEnabled)
            {
                this.isEnabled = isEnabled;
            }

        }


        @Override
        public String toString()
        {
            return toString(false);
        }


        public String toString(boolean includeUnit)
        {
            String valueStr = value;

            if ((includeUnit) && (unit != null))
            {
                valueStr += " " + unit;
            }

            return valueStr;
        }


        public void setValue(String value)
        {
//        // This might be useful for detecting special values/modes
//        /*
//        try
//        {
//            Double subValue = Double.parseDouble(subDigits);
//            //System.out.println("Sub value:     [ " + subDigits + " ]    " + subValue.toString());
//        }
//        catch (NumberFormatException nfe)
//        {
//            if (subDigits.contains("0L") || subDigits.contains("0.L")) // Just 'L' good enough?
//            {
//                //System.out.println("Sub value:     [ " + subDigits + " ]    OL");
//            }
//            else if (subDigits.contains("0PEn"))
//            {
//                //System.out.println("Sub value:     [ " + subDigits + " ]    OPEN");
//            }
//            else
//            {
//                //System.out.println("Sub value:     [ " + subDigits + " ]    ?");
//            }
//        }
//        */

            // Update the datetime.
            lastUpdate = new Date();

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
         * Get the value represented as a String that resembles what is
         * displayed on the LCD.
         *
         * This also includes non-numerical values (i.e. words on LCD like
         * "Shrt", "OPEn").
         * <p>
         * For an exact representation (including whitespace), use the
         * {@link #getValueVerbatim() getValueVerbatim} method.
         * <p>
         * For a numerical value, {@link #getValueDouble() getValueDouble} use
         * the method.
         *
         * @return
         */
        public String getValue()
        {
            return value;
        }


        /**
         * Gets a more accurate representation what the value looks like on the
         * Protek608 LCD including whitespace padding, etc.
         *
         * This also includes non-numerical values (i.e. words on LCD like
         * "Shrt", "OPEn").
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
         * @return a Double value if numerical, or null if not-numerical (i.e.
         *         words on LCD like "Shrt", "OPEn").
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


        public void setUnit(String unit)
        {
            this.unit = unit.trim();
        }


        public String getUnit()
        {
            return unit;
        }


        /**
         * Match a number with optional '-' and decimal.
         *
         * Note: Will fail if non-latin (i.e. 0 to 9) digits used (for example,
         * arabic digits).
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

    public Flags flags = new Flags();


    /**
     * Flags are modes or options that the Protek 608 DMM can have enabled or
     * disabled.
     *
     * These are usually displayed on the LCD, and the status of each is
     * available as a boolean in this class.
     */
    public class Flags
    {
        public boolean autoOff;
        public boolean pulse;
        public boolean max;
        public boolean posPeak;
        public boolean rel;
        public boolean recall;
        public boolean goNg;
        public boolean posPercent;
        public boolean rs232;
        public boolean pos;
        public boolean neg;
        public boolean min;
        public boolean negPeak;
        public boolean avg;
        public boolean store;
        public boolean ref;
        public boolean negPercent;
        public boolean range;
        public boolean hold;
        public boolean duty;
        public boolean audio;
        public boolean diode;
    }

}
