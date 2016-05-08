package me.whichapp.newdeletedsongs;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Dexter on 04-Apr-16.
 */
public class MusicItem implements Parcelable {
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

    protected MusicItem(Parcel in) {
    id = in.readString();
    path = in.readString();
    title = in.readString();
    timestamp = in.readString();
    info = in.readInt();
    }

    @Override
    public int describeContents() {
    return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(id);
    dest.writeString(path);
    dest.writeString(title);
    dest.writeString(timestamp);
    dest.writeInt(info);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<MusicItem> CREATOR = new Parcelable.Creator<MusicItem>() {
        @Override
        public MusicItem createFromParcel(Parcel in) {
            return new MusicItem(in);
        }

        @Override
        public MusicItem[] newArray(int size) {
            return new MusicItem[size];
        }
    };
}
