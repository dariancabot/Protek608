package com.dariancabot.protek608;

import java.util.Date;
import static org.hamcrest.Matchers.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;


/**
 *
 * @author Darian Cabot
 */
public class DataTest
{

    public DataTest()
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


    @Test
    public void testBasicSetAndGet()
    {
        Data data = new Data();

        // Test text value
        data.mainValue.setValue("  A b c d ");
        assertThat(data.mainValue.getValueVerbatim(), equalTo("  A b c d "));
        assertThat(data.mainValue.getValue(), equalTo("A b c d"));
        assertThat(data.mainValue.getValueDouble(), equalTo(null));

        // Test numerical value
        data.mainValue.setValue("  -123.456 ");

        // Test lastUpdate Date
        Date setDate = new Date();
        Long timeDiff = setDate.getTime() - data.lastUpdate.getTime();
        assertThat(timeDiff.intValue(), is(lessThan(100)));

        assertThat(data.mainValue.getValueVerbatim(), equalTo("  -123.456 "));
        assertThat(data.mainValue.getValue(), equalTo("-123.456"));
        assertThat(data.mainValue.getValueDouble(), equalTo( - 123.456));

        // Test toString() methods
        assertThat(data.mainValue.toString(), equalTo("-123.456"));
        assertThat(data.mainValue.toString(false), equalTo("-123.456"));
        assertThat(data.mainValue.toString(true), equalTo("-123.456"));

        // Test unit
        data.mainValue.setUnit(" mV ");
        assertThat(data.mainValue.getUnit(), equalTo("mV"));

        // Test toString() methods
        assertThat(data.mainValue.toString(), equalTo("-123.456"));
        assertThat(data.mainValue.toString(false), equalTo("-123.456"));
        assertThat(data.mainValue.toString(true), equalTo("-123.456 mV"));

    }


    @Test
    public void testStatistics()
    {
        // TODO: Test time duration.

        Data data = new Data();

        // Statistics not enabled yet, check null/default...
        assertThat(data.mainValue.statistics.getMinimum(), equalTo(null));
        assertThat(data.mainValue.statistics.getMaximum(), equalTo(null));
        assertThat(data.mainValue.statistics.getAverage(), equalTo(null));
        assertThat(data.mainValue.statistics.getSamples(), equalTo(0L));

        // Enable statistics.
        data.mainValue.statistics.setIsEnabled(true);

        // Loop in a bunch of values...
        for (int i = 1000; i <= 3000; i ++)
        {
            data.mainValue.setValue(String.valueOf(i));
        }

        assertThat(data.mainValue.statistics.getMinimum(), equalTo(1000d));
        assertThat(data.mainValue.statistics.getMaximum(), equalTo(3000d));
        assertThat(data.mainValue.statistics.getAverage(), equalTo(2000d));
        assertThat(data.mainValue.statistics.getSamples(), equalTo(2001L));

        // Reset statistics.
        data.mainValue.statistics.reset();

        // Statistics reset, check null/default...
        assertThat(data.mainValue.statistics.getMinimum(), equalTo(null));
        assertThat(data.mainValue.statistics.getMaximum(), equalTo(null));
        assertThat(data.mainValue.statistics.getAverage(), equalTo(null));
        assertThat(data.mainValue.statistics.getSamples(), equalTo(0L));

        // Statistics should still be enabled. Loop in a bunch of values...
        for (int i = 1000; i <= 3000; i ++)
        {
            data.mainValue.setValue(String.valueOf(i));
        }

        assertThat(data.mainValue.statistics.getMinimum(), equalTo(1000d));
        assertThat(data.mainValue.statistics.getMaximum(), equalTo(3000d));
        assertThat(data.mainValue.statistics.getAverage(), equalTo(2000d));
        assertThat(data.mainValue.statistics.getSamples(), equalTo(2001L));

        // Disable statistics.
        data.mainValue.statistics.setIsEnabled(false);

        // Set value, this should not be counted on statistics.
        data.mainValue.setValue("-50");
        data.mainValue.setValue("50000");

        // Check that nothing changed...
        assertThat(data.mainValue.statistics.getMinimum(), equalTo(1000d));
        assertThat(data.mainValue.statistics.getMaximum(), equalTo(3000d));
        assertThat(data.mainValue.statistics.getAverage(), equalTo(2000d));
        assertThat(data.mainValue.statistics.getSamples(), equalTo(2001L));

        // Enable statistics.
        data.mainValue.statistics.setIsEnabled(true);

        // Set value, this should be counted on statistics.
        data.mainValue.setValue("-50");
        data.mainValue.setValue("50000");

        // Check that the new values were applied...
        assertThat(data.mainValue.statistics.getMinimum(), equalTo( - 50d));
        assertThat(data.mainValue.statistics.getMaximum(), equalTo(50000d));
        assertEquals(data.mainValue.statistics.getAverage(), 2022.94058, 0.00001);
        assertThat(data.mainValue.statistics.getSamples(), equalTo(2003L));

    }

}
