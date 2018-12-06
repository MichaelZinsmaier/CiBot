package ccp.cibot.circuitwrapper.dto;

public class ConversationItem
{
    public String type;
    public String itemId;

    // might be empty
    public TextItem text;

    public long modificationTime;

    public String creatorId;

    // unnecessary fields omitted
}
