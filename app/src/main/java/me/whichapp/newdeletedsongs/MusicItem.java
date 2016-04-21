package me.whichapp.newdeletedsongs;

/**
 * Created by Dexter on 04-Apr-16.
 */
public class MusicItem
{
    private String id;
    private String path;
    private String title;
    private String timestamp;
    private int info;

    public MusicItem(String id, String title, String path, int info)
    {
        this.id = id;
        this.path = path;
        this.title = title;
        this.info = info;
    }

    public MusicItem(String id, String content, String number, String timestamp, int info)
    {
        this(id, content, number, info);
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return this.getTitle() + " # " + this.getInfo() + " # " + this.getId() + " # " + this.getPath() ;
    }

    public int getInfo() {
        return info;
    }
    public String getPath() {
        return path;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setInfo(int info) {
        this.info = info;
    }

    public String getTitle() {
        return title;
    }

    public String getTimestamp()
    {
        return timestamp;
    }
}
