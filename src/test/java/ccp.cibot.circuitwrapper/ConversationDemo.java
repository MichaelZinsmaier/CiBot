package ccp.cibot.circuitwrapper;

import ccp.cibot.circuitwrapper.dto.ConversationItem;
import ccp.cibot.circuitwrapper.dto.ConversationResponse;
import java.util.List;
import java.util.stream.Collectors;

public class ConversationDemo
{

    public static void main(String[] args)
    {
       String client_id = args[0];
       String client_secret = args[1];
       String convId = args[2];
       String itemId = args[3];

        RestSettings restSettings = new RestSettings("url", "proxy", 81);

       TokenEndpoint tke = new TokenEndpoint(restSettings, client_id, client_secret);
       ConversationEndpoint convEndpoint = new ConversationEndpoint(restSettings, convId);

       ConversationResponse conversation = convEndpoint.getConversation(tke.getToken());
       System.out.println("topic is " + conversation.topic);

       List<ConversationItem> items = convEndpoint.listConversationItems(tke.getToken());
       List<String> subjects = items.stream()
            .filter(item -> item.type.equals("TEXT"))
            .filter(item -> item.text.subject != null)
            .map(item -> item.text.subject)
            .collect(Collectors.toList());

       for (String sub : subjects)
       {
           System.out.println(sub);
       }

       // add to an existing item
       new ConversationEndpoint(restSettings, convId).addMessageToItem("hi from JODD adding to item " + itemId, itemId, tke.getToken());

       // create a new subject and add to it
       new ConversationEndpoint(restSettings, convId).addMessageToConversation( "new subject","hi from JODD creating my own subject", tke.getToken());


    }

}
