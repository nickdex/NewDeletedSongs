package me.whichapp.newdeletedsongs;

/**
 * Created by nick on 17/3/16.
 */
public class ContactItem
{
    private String id;
    private String number;
    private String content;
    private String timestamp;
    private int info;

    public ContactItem(String id, String content, String number, int info)
    {
        this.number = number;
        this.id = id;
        this.content = content;
        this.info = info;
    }

    public ContactItem(String id, String content, String number, String timestamp, int info)
    {
        this(id, content, number, info);
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return this.getContent() + " # " + this.getInfo() + " # " + this.getNumber() + " # " + this.getId();
    }

    public int getInfo() {
        return info;
    }

    public String getId()
    {
        return id;
    }

    public String getNumber()
    {
        return number;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setInfo(int info) {
        this.info = info;
    }

    public String getContent() {
        return content;
    }

    public String getTimestamp()
    {
        return timestamp;
    }
}
