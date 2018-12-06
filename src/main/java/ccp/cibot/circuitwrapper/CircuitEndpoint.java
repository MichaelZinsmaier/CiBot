package ccp.cibot.circuitwrapper;

import ccp.cibot.circuitwrapper.dto.ConversationMatch;
import ccp.cibot.circuitwrapper.dto.ConversationSearchResponse;
import java.util.List;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.http.net.SocketHttpConnectionProvider;
import jodd.json.JsonParser;

public class CircuitEndpoint
{

    private static final String SEARCH_CONVERSATION = "/rest/v2/conversations/search?term=%s&includeItemIds=false";

    private final RestSettings restSettings;
    private final SocketHttpConnectionProvider connectionProvider;

    public CircuitEndpoint(RestSettings restSettings)
    {
        this.restSettings = restSettings;
        connectionProvider = Util.createHttpConnectionProvider(restSettings);
    }

    public List<ConversationMatch> searchConversationFullText(String conversationName, String token)
    {
        HttpResponse response = HttpRequest
                        .get(restSettings.getCircuitUrl() + String.format(SEARCH_CONVERSATION, conversationName))
                        .withConnectionProvider(connectionProvider)
                        .header("Authorization", token)
                        .send();


        if (response.statusCode() != 200) {
            throw new WrongStatusException(getClass().getSimpleName() + "/searchConversationFullText", 200, response.statusCode());
        }
        ConversationSearchResponse result = new JsonParser().parse(response.body(), ConversationSearchResponse.class);
        response.close();

        return result.matchingConversations;
    }
}
