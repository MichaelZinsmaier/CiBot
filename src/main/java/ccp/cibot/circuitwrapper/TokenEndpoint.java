package ccp.cibot.circuitwrapper;/* Copyrights owned by Atos and Siemens, 2018. */

import ccp.cibot.circuitwrapper.dto.TokenResponse;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.http.net.SocketHttpConnectionProvider;
import jodd.json.JsonParser;

public class TokenEndpoint
{
    private static final String AUTHENTICATE_FOR_TOKEN = "oauth/token";

    private final RestSettings restSettings;
    private final String clientId;
    private final String clientSecret;
    private final SocketHttpConnectionProvider connectionProvider;

    public TokenEndpoint(RestSettings restSettings, String clientId, String clientSecret)
    {

        this.restSettings = restSettings;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        connectionProvider = Util.createHttpConnectionProvider(restSettings);
    }

    public String getToken()
    {
        HttpResponse response = HttpRequest
                        .post(restSettings.getCircuitUrl()+ AUTHENTICATE_FOR_TOKEN)
                        .withConnectionProvider(connectionProvider)
                        .form("client_id", clientId)
                        .form("client_secret", clientSecret)
                        .form("scope", "WRITE_CONVERSATIONS,READ_CONVERSATIONS")
                        .form("grant_type", "client_credentials")
                        .send();

        if (response.statusCode() != 200) {
            throw new WrongStatusException(getClass().getSimpleName() + "/getToken", 200, response.statusCode());
        }

        TokenResponse token = new JsonParser().parse(response.body(), TokenResponse.class);
        response.close();

        return "Bearer " + token.access_token;
    }
}
