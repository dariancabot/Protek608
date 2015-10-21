package com.dariancabot.protek608;

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
public class EventListenerTest
{
    private int lastEventData = 12;


    public EventListenerTest()
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


    /**
     * Test of dataUpdateEvent method, of class EventListener.
     */
    @Test
    public void testDataUpdateEvent()
    {
        EventListener instance = new EventListenerImpl();

        assertThat("Initialisation of test variable failed", lastEventData, equalTo(12));

        instance.dataUpdateEvent(); // Trigger event.

        assertThat("Event failed to update test variable", lastEventData, equalTo(34));

    }


    public class EventListenerImpl implements EventListener
    {
        @Override
        public void dataUpdateEvent()
        {
            lastEventData = 34;
        }
    }

}
