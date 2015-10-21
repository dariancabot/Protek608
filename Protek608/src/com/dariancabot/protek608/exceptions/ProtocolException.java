package com.dariancabot.protek608.exceptions;


/**
 *
 * @author Darian Cabot
 */
public class ProtocolException extends RuntimeException
{

    public ProtocolException()
    {
        super();
    }


    public ProtocolException(String message)
    {
        super(message);
    }


    public ProtocolException(String message, Throwable cause)
    {
        super(message, cause);
    }


    @Override
    public String toString()
    {
        return super.toString();
    }


    @Override
    public String getMessage()
    {
        return super.getMessage();
    }

}
