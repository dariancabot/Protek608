package com.dariancabot.protek608;

import java.util.Date;
import static org.hamcrest.Matchers.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

// TODO: Testing of data statistics (load values with loop?).

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
    public void testSomeMethod()
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

}
