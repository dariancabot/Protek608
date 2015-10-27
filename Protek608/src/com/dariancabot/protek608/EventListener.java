package com.dariancabot.protek608;


/**
 * The EventListener interface is used to notify of successfully received data from the DMM.
 *
 * @author Darian Cabot
 */
public interface EventListener
{

    /**
     * This method is called when data is received from the DMM.
     *
     * <p>
     * More specifically, the received packet must be valid, then decoded, then the Data Object is updated, *then* this method is called.
     */
    public void dataUpdateEvent();

}
