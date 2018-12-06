package ccp.cibot.circuitwrapper;

import ccp.cibot.circuitwrapper.dto.ConversationMatch;
import java.util.List;

public class CircuitDemo
{
    public static void main(String[] args)
    {
        String client_id = args[0];
        String client_secret = args[1];
        RestSettings restSettings = new RestSettings("url", "proxy", 81);

        TokenEndpoint tke = new TokenEndpoint(restSettings, client_id, client_secret);
        CircuitEndpoint circuitEndpoint = new CircuitEndpoint(restSettings);

        List<ConversationMatch> matches = circuitEndpoint.searchConversationFullText("Jenkins Test", tke.getToken());

        for (ConversationMatch match : matches)
        {
            System.out.println(match.convId);
        }
    }
}
