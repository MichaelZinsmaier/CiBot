package ccp.cibot.circuitwrapper;

import ccp.cibot.circuitwrapper.dto.ConversationItem;
import ccp.cibot.circuitwrapper.dto.ConversationResponse;
import java.util.List;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.http.net.SocketHttpConnectionProvider;
import jodd.json.JsonParser;

public class ConversationEndpoint
{
    private static final int MAX_ITEMS = 100; // curent limit

    private static final String ADD_MESSAGE_TO_ITEM = "rest/v2/conversations/%s/messages/%s";
    private static final String ADD_MESSAGE_TO_CONVERSATION = "rest/v2/conversations/%s/messages";
    private static final String GET_CONVERSATION_DETAILS = "rest/v2/conversations/%s";
    private static final String LIST_CONVERSATION_ITEMS = "rest/v2/conversations/%s/items?results=" + MAX_ITEMS;

    private final String convId;
    private final SocketHttpConnectionProvider connectionProvider;
    private final RestSettings restSettings;

    public ConversationEndpoint(RestSettings restSettings, String convId)
    {

        this.restSettings = restSettings;
        this.convId = convId;
        connectionProvider = Util.createHttpConnectionProvider(restSettings);
    }

    public void addMessageToConversation(String subject, String text, String token)
    {
        HttpResponse response = HttpRequest
                        .post(restSettings.getCircuitUrl() + String.format(ADD_MESSAGE_TO_CONVERSATION, convId))
                        .withConnectionProvider(connectionProvider)
                        .header("Authorization", token)
                        .form("subject", subject)
                        .form("content", text)
                        .send();

        if (response.statusCode() != 200) {
            throw new WrongStatusException(getClass().getSimpleName() + "/addMessageToConversation", 200, response.statusCode());
        }
        response.close();
    }

    public void addMessageToItem(String text, String itemId, String token)
    {
        HttpResponse response = HttpRequest
                        .post(restSettings.getCircuitUrl() + String.format(ADD_MESSAGE_TO_ITEM, convId, itemId))
                        .withConnectionProvider(connectionProvider)
                        .header("Authorization", token)
                        .form("content", text)
                       .send();

        if (response.statusCode() != 200) {
            throw new WrongStatusException(getClass().getSimpleName() + "/addMessageToItem", 200, response.statusCode());
        }
        response.close();
    }

    public ConversationResponse getConversation(String token)
    {
        HttpResponse response = HttpRequest
                        .get(restSettings.getCircuitUrl() + String.format(GET_CONVERSATION_DETAILS, convId))
                        .withConnectionProvider(connectionProvider)
                        .header("Authorization", token)
                        .send();

        if (response.statusCode() != 200) {
            throw new WrongStatusException(getClass().getSimpleName() + "/getConversation", 200, response.statusCode());
        }
        ConversationResponse conversation = new JsonParser().parse(response.body(), ConversationResponse.class);
        response.close();

        return conversation;
    }

    public List<ConversationItem> listConversationItems(String token)
    {
        HttpResponse response = HttpRequest
                        .get(restSettings.getCircuitUrl() + String.format(LIST_CONVERSATION_ITEMS, convId))
                        .withConnectionProvider(connectionProvider)
                        .header("Authorization", token)
                        .send();

        if (response.statusCode() != 200) {
            throw new WrongStatusException(getClass().getSimpleName() + "/listConversationItems", 200, response.statusCode());
        }
        List<ConversationItem> items = new JsonParser().parseAsList(response.body(), ConversationItem.class);
        response.close();

        return items;
    }
}
