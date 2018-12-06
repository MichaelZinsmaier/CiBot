package ccp.cibot;

import ccp.cibot.circuitwrapper.ConversationEndpoint;
import ccp.cibot.circuitwrapper.RestSettings;
import ccp.cibot.circuitwrapper.TokenEndpoint;
import ccp.cibot.circuitwrapper.dto.ConversationItem;
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
import java.util.Optional;
import java.util.logging.Logger;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

public class CiNotificationBot extends Notifier
{
	private static final Logger LOGGER = Logger.getLogger(CiNotificationBot.class.getName());

	private final String conversationId;
	private final String username;
	private final String password;

	@DataBoundConstructor
	public CiNotificationBot(String conversationId, String username, String password)
	{
		this.conversationId = conversationId;
		this.username = username;
		this.password = password;
	}

	public String getConversationId()
	{
		return conversationId;
	}

	public String getUsername()
	{
		return username;
	}

	public String getPassword()
	{
		return password;
	}

	private RestSettings getRestSettings()
	{
		String circuitEndpoint = getDescriptor().getCircuitEndpoint();
		String proxyUrl = getDescriptor().getProxyUrl();
		int port = Integer.valueOf(getDescriptor().getProxyPort());

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
		LOGGER.info("running Circuit Bot integration for current build");
		listener.getLogger().println("CiNotificationBot: sending build results to Circuit");

		try
		{
			// read and expand parameters
			RestSettings restSettings = getRestSettings();
			EnvVars env = build.getEnvironment(listener);

			String expConversationId = env.expand(conversationId);
			String expUser = env.expand(username);
			String expPassword = env.expand(password);

			// interact with circuit

			// get token
			TokenEndpoint tokenEndpoint = new TokenEndpoint(restSettings, expUser, expPassword);
			String token = tokenEndpoint.getToken();

			// search fitting subject and post build status
			String subject = build.getProject().getFullName();
			MessageGenerator messageGenerator = new MessageGenerator();

			ConversationEndpoint conversationEndpoint = new ConversationEndpoint(restSettings, expConversationId);
			Optional<ConversationItem> topic = conversationEndpoint.listConversationItems(token).stream()
							.filter(item -> item.type.equals("TEXT"))                                        // text items
							.filter(item -> item.text.subject != null)                                       // subjects
							.sorted((i1, i2) -> Long.compare(i2.modificationTime, i1.modificationTime))      // newest first
							.filter(item -> subject.equals(item.text.subject))                               // subject with job name ?
							.findFirst();

			// either create a new subject with the job name or append to an existing one if found
			if (topic.isPresent())
			{
				ConversationItem parent = topic.get();
				JobStatusAnalyzer analyzer = new JobStatusAnalyzer(
								() -> conversationEndpoint.addMessageToItem(messageGenerator.buildFailed(build), parent.itemId, token),
								() -> conversationEndpoint.addMessageToItem(messageGenerator.buildUnstable(build), parent.itemId, token),
								() -> conversationEndpoint.addMessageToItem(messageGenerator.buildSuccess(build), parent.itemId, token));

				analyzer.analyze(build, listener);
			} else {
				JobStatusAnalyzer analyzer = new JobStatusAnalyzer(
								() -> conversationEndpoint.addMessageToConversation(subject, messageGenerator.buildFailed(build), token),
								() -> conversationEndpoint.addMessageToConversation(subject, messageGenerator.buildUnstable(build), token),
								() -> conversationEndpoint.addMessageToConversation(subject, messageGenerator.buildSuccess(build), token));

				analyzer.analyze(build, listener);
			}


		} catch (Exception ex) {
			String msg = "something went wrong " + ex;
			listener.getLogger().println("CiNotificationBot: " + msg);
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
			super(CiNotificationBot.class);
			load();
		}

		@Override
		public boolean configure(final StaplerRequest req, final JSONObject json) throws FormException
		{
			JSONObject ciJson = json.getJSONObject("cibotmes");
			circuitEndpoint = ciJson.getString("circuitEndpoint");

			try {
				proxyUrl = ciJson.getString("proxyUrl");
				proxyPort = ciJson.getInt("proxyPort");
			} catch (JSONException ex) {
				LOGGER.warning("Configuration of proxy settings failed for CiNotificationBot " + ex.getMessage());
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
			return "CircuitBot - Chat Notification";
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
