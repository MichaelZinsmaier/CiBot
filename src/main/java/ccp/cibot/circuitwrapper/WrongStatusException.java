package ccp.cibot.circuitwrapper;

import jodd.exception.UncheckedException;

public class WrongStatusException extends UncheckedException
{
    public WrongStatusException(String endpoint, int expected, int got)
    {
        super(String.format("Endpoint: %s, Status code didn't match, expected %s but got %s", endpoint, expected, got));
    }
}
