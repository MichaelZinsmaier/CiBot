package ccp.cibot.circuitwrapper;

public class RestSettings
{

    private final String circuitUrl;
    private final String proxyUrl;
    private final int proxyPort;


    public RestSettings(String circuitUrl, String proxyUrl, int proxyPort)
    {
        this.circuitUrl = circuitUrl;
        this.proxyUrl = proxyUrl;
        this.proxyPort = proxyPort;
    }


    public String getCircuitUrl()
    {
        return circuitUrl;
    }

    public String getProxyUrl()
    {
        return proxyUrl;
    }

    public int getProxyPort()
    {
        return proxyPort;
    }


}
