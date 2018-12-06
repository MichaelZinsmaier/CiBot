package ccp.cibot.circuitwrapper;

import jodd.http.ProxyInfo;
import jodd.http.net.SocketHttpConnectionProvider;

public class Util
{

    public static SocketHttpConnectionProvider createHttpConnectionProvider(RestSettings restSettings)
    {
        SocketHttpConnectionProvider provider = new SocketHttpConnectionProvider();

        if (restSettings.getProxyUrl() != null && !restSettings.getProxyUrl().isEmpty() && restSettings.getProxyPort() > 0)
        {
            provider.useProxy(ProxyInfo.httpProxy(restSettings.getProxyUrl(), restSettings.getProxyPort(), null, null));
        }

        return provider;
    }
}
