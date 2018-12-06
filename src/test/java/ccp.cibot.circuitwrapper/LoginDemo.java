package ccp.cibot.circuitwrapper;

public class LoginDemo
{

    public static void main(String[] args)
    {
       String client_id = args[0];
       String client_secret = args[1];

        RestSettings restSettings = new RestSettings("url", "proxy", 81);

       TokenEndpoint tke = new TokenEndpoint(restSettings, client_id, client_secret);
       System.out.println(tke.getToken());
    }

}
