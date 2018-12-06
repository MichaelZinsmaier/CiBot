package ccp.cibot;

import ccp.cibot.circuitwrapper.ConversationEndpoint;
import ccp.cibot.circuitwrapper.RestSettings;
import ccp.cibot.circuitwrapper.TokenEndpoint;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import java.util.logging.Logger;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class CiMessageBot extends Notifier
{
    private static final Logger LOGGER = Logger.getLogger(CiNotificationBot.class.getName());

    private final String conversationId;
    private final String message;
    private final String username;
    private final String password;
    private final String send;

    @DataBoundConstructor
    public CiMessageBot(String conversationId,
                    String username,
                    String password,
                    String message,
                    String send
    )
    {
        this.conversationId = conversationId;
        this.username = username;
        this.password = password;
        this.message = message;
        this.send = send;
    }

    public String getConversationId()
    {
        return conversationId;
    }

    public String getMessage() { return message; }

    public String getUsername()
    {
        return username;
    }

    public String getPassword()
    {
        return password;
    }

    public String getSend() { return send; }

    private RestSettings getRestSettings()
    {
        String circuitEndpoint = getDescriptor().getCircuitEndpoint();
        String proxyUrl = getDescriptor().getProxyUrl();
        int port = getDescriptor().getProxyPort();

        return new RestSettings(circuitEndpoint, proxyUrl, port);
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }
    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener)
    {
        LOGGER.info("running Circuit Message Bot");
        listener.getLogger().println("CiMessageBot: sending message to Circuit");

        try
        {
            // read and expand parameters
            RestSettings restSettings = getRestSettings();
            EnvVars env = build.getEnvironment(listener);

            String expConversationId = env.expand(conversationId);
            String expMessage = env.expand(message);
            String expSend = env.expand(send);
            String expUser = env.expand(username);
            String expPassword = env.expand(password);

            // interact with circuit
            String compSend = expSend.toLowerCase();

            if (compSend.equals("no") || compSend.equals("inactive"))
            {
                String msg = "will not send out a message (send field is 'no' or 'inactive')";
                LOGGER.info(msg);
                listener.getLogger().println("CiMessageBot: " + msg);
                return true;
            } else if (!compSend.equals("yes") && !compSend.equals("send") && !compSend.equals("active"))
            {
                String msg = "could not parse the send field, legal values are yes, send, active and no, inactive";
                LOGGER.warning(msg);
                listener.getLogger().println("CiMessageBot: " + msg);
                return false;
            }

            // ok we actually send it


            // get token
            TokenEndpoint tokenEndpoint = new TokenEndpoint(restSettings, expUser, expPassword);
            String token = tokenEndpoint.getToken();

            // search fitting subject and post build status
            ConversationEndpoint conversationEndpoint = new ConversationEndpoint(restSettings, expConversationId);
            conversationEndpoint.addMessageToConversation("CiNMessageBot - AutoMessage", expMessage, token);

        } catch (Exception ex) {
            String msg = "something went wrong " + ex;
            listener.getLogger().println("CiMessageBot: " + msg);
            LOGGER.warning(msg);

            return false;
        }

        return true;
    }



    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher>
    {
        private static final Logger LOGGER = Logger.getLogger(DescriptorImpl.class.getName());

        private String circuitEndpoint;
        private String proxyUrl;
        private int proxyPort;

        public DescriptorImpl() {
            super(CiMessageBot.class);
            load();
        }

        @Override
        public boolean configure(final StaplerRequest req, final JSONObject json) throws FormException
        {
            JSONObject ciJson = json.getJSONObject("cibotnot");
            circuitEndpoint = ciJson.getString("circuitEndpoint");

            try {
                proxyUrl = ciJson.getString("proxyUrl");
                proxyPort = ciJson.getInt("proxyPort");
            } catch (JSONException ex) {
                LOGGER.warning("Configuration of proxy settings failed for CiMessageBot " + ex.getMessage());
                proxyUrl = "";
                proxyPort = -1;
            }

            save();
            return true;
        }

        @Override public boolean isApplicable(Class<? extends AbstractProject> aClass)
        {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "CircuitBot - Send Message";
        }

        public String getCircuitEndpoint()
        {
            return circuitEndpoint;
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
}
