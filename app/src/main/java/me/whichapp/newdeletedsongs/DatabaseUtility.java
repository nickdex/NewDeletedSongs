package me.whichapp.newdeletedsongs;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Dexter on 06-Apr-16.
 */
public class DatabaseUtility extends SQLiteOpenHelper
{
    private static final String NAME = "ping.db";

    private static final int VERSION = 1;
    private static final String TAG = "DatabaseUtility";

    public final String CONTACT_TABLE = "CONTACT";
    public final String MUSIC_TABLE = "MUSIC";

    private final String ID = "_ID";
    private final String TITLE = "NAME";
    private final String PATH = "PATH";
    private final String NUMBER = "NUMBER";
    private final String IS_NEW = "IS_NEW";
    private final String TIMESTAMP = "TIMESTAMP";

    public final static int OLD = 0;
    public final static int NEW = 1;
    public final static int DELETED = 2;
    public final static int CHANGED = 3;

    public DatabaseUtility(Context context)
    {
        super(context, NAME, null, VERSION);

    }

    public static DatabaseUtility newInstance(Context context)
    {
        return new DatabaseUtility(context);
    }


    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + MUSIC_TABLE + " ( " + ID + " TEXT PRIMARY KEY, " + TITLE + " TEXT, " + PATH + " TEXT, " + TIMESTAMP + " TEXT, " + IS_NEW + " INTEGER);");
        Log.v(TAG, "Database is created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + MUSIC_TABLE);
        onCreate(db);
    }


    public void insertMusicList(List<MusicItem> fresh)
    {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(MUSIC_TABLE, new String[]{ID, TITLE, PATH, IS_NEW}, null, null, null, null, null);

        if (cursor != null)
        {
            if (cursor.getCount() == 0)
            {
                for (MusicItem item : fresh)
                {
                    item.setInfo(NEW);
                    insertMusicItem(item, db);
                }
            } else
            {
                for (MusicItem item : fresh)
                {
                    insertMusicItem(item, db);
                }

                String itemString = getSQLNotInListAsString(fresh);

                //Gives Deleted Items
                cursor = db.query(MUSIC_TABLE, new String[]{ID, TITLE, PATH, IS_NEW}, ID+ " NOT IN "+itemString, null, null, null, null);

                List<MusicItem> deletedItems = getMusicListFromDatabaseCursorToInsert(cursor);

                if (deletedItems != null)
                {
                    for (MusicItem item : deletedItems)
                    {
                        item.setInfo(DELETED);
                        insertDeletedMusicItem(item, db);
                    }
                }
                else
                {
                    Log.i(TAG, "No Deleted Items");
                }
            }
        }

        db.close();
    }

    public String getSQLNotInListAsString(List<MusicItem> list)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for(MusicItem item : list)
        {
            builder.append("'");
            builder.append(item.getId());
            builder.append("'");
            builder.append(",");
        }
        builder.delete(builder.length()-1, builder.length());
        builder.append(")");

        return builder.toString();
    }

    public List<MusicItem> getDeletedMusicList()
    {
        return getSpecialisedMusicList(DELETED);
    }

    public List<MusicItem> getNewMusicList()
    {
        return getSpecialisedMusicList(NEW);
    }

    private List<MusicItem> getSpecialisedMusicList(int info_type)
    {
        SQLiteDatabase db = getReadableDatabase();
        List<MusicItem> list = new ArrayList<>();
        Cursor cursor = db.query(MUSIC_TABLE, null, IS_NEW + " = ?",new String[]{String.valueOf(info_type)}, null, null, TITLE);
        while(cursor.moveToNext())
        {
            String id = cursor.getString(cursor.getColumnIndex(ID));
            String title = cursor.getString(cursor.getColumnIndex(TITLE));
            String path = cursor.getString(cursor.getColumnIndex(PATH));
            String timestamp = cursor.getString(cursor.getColumnIndex(TIMESTAMP));
            int info = cursor.getInt(cursor.getColumnIndex(IS_NEW));

            MusicItem item = new MusicItem(id, title, path, timestamp, info);
            list.add(item);
        }
        cursor.close();
        db.close();
        return list;
    }


    public List<MusicItem> getMusicItemListFromDatabase()
    {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor;
        cursor = db.query(MUSIC_TABLE, new String[]{ID, TITLE, PATH, IS_NEW}, null, null, null, null, null);

        if (cursor != null)
        {
            return getSortedMusicList(getMusicListFromDatabaseCursorToShow(cursor, db, MUSIC_TABLE));
        }

        db.close();
        return null;
    }

    private List<MusicItem> getSortedMusicList(List<MusicItem> list)
    {
        List<MusicItem> tempList = new ArrayList<>();
        for (MusicItem item : list)
        {
            if (item.getInfo() == OLD)
            {
                tempList.add(item);
            }
        }

        list.removeAll(tempList);
        list.addAll(tempList);

        return list;
    }



    private List<MusicItem> getUpdatedMusicItemList(List<MusicItem> old, List<MusicItem> fresh)
    {
        List<MusicItem> oldItems = new ArrayList<>();
        List<MusicItem> changedItems = new ArrayList<>();

        //New Item or Changed
        if (fresh.size() >= old.size())
        {
//            flag = NEW;
            for (MusicItem itemFresh : fresh)
            {
                for (MusicItem itemOld : old)
                {
                    if (itemFresh.getId().equals(itemOld.getId()))
                    {
                        if (itemFresh.getTitle().equals(itemOld.getTitle()))
                        {
                            oldItems.add(itemFresh);
                        } else
                        {
                            changedItems.add(itemFresh);
                        }
                        break;
                    }
                }
            }

            fresh.removeAll(oldItems);
            fresh.removeAll(changedItems);

            for (MusicItem item : fresh)
            {
                item.setInfo(NEW);
            }
            for (MusicItem item : changedItems)
            {
                item.setInfo(CHANGED);
            }

            fresh.addAll(changedItems);

            return fresh;
        }
        //Deleted Item
        else if (fresh.size() < old.size())
        {
            for (MusicItem itemOld : old)
            {
                for (MusicItem itemFresh : fresh)
                {
                    if (itemFresh.getId().equals(itemOld.getId()))
                    {
                        oldItems.add(itemOld);
                        break;
                    }
                }
            }

            old.removeAll(oldItems);

            for (MusicItem item : old)
            {
                item.setInfo(DELETED);
            }

            return old;
        }

        return null;
    }

    private List<MusicItem> getMusicListFromDatabaseCursorToInsert(Cursor cursor)
    {
        if (cursor != null && cursor.getCount() > 0)
        {
            List<MusicItem> list = new ArrayList<>();
            while (cursor.moveToNext())
            {
                String id = cursor.getString(0);
                String title = cursor.getString(1);
                String path = cursor.getString(2);
                int info = cursor.getInt(3);
                MusicItem item = new MusicItem(id, title, path, info);
                list.add(item);
            }

            return list;
        } else
        {
            return null;
        }
    }


    private List<MusicItem> getMusicListFromDatabaseCursorToShow(Cursor cursor, SQLiteDatabase db, String table)
    {
        if (cursor != null && cursor.getCount() > 0)
        {
            List<MusicItem> list = new ArrayList<>();
            while (cursor.moveToNext())
            {
                String id = cursor.getString(0);
                String title = cursor.getString(1);
                String path = cursor.getString(2);
                int info = cursor.getInt(3);
                MusicItem item = new MusicItem(id, title, path, info);
                list.add(item);

                if (info == DELETED)
                {
                    db.delete(table, IS_NEW + " = ?", new String[]{String.valueOf(info)});
                }
            }

            return list;
        } else
        {
            return null;
        }
    }


    public List<MusicItem> getMusicListFromCursor(Cursor cursor)
    {
        if (cursor != null && cursor.getCount() > 0)
        {
            List<MusicItem> list = new ArrayList<>();
            while (cursor.moveToNext())
            {

                String id = cursor.getString(0);
                String title = cursor.getString(1);
                String path = cursor.getString(2);
                //OLD doesn't matter it will be replaced later when interacting with database
                MusicItem item = new MusicItem(id, title, path, NEW);
                list.add(item);
            }

            if (!list.isEmpty())
            {
                return list;
            }

            cursor.close();
        }

        return null;

    }


    private void insertMusicItem(MusicItem item, SQLiteDatabase database)
    {

        ContentValues values = new ContentValues();
        values.put(ID, item.getId());
        values.put(TITLE, item.getTitle());
        values.put(PATH, item.getPath());
        SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
        values.put(TIMESTAMP, currentDate.format(new Date()));
        values.put(IS_NEW, item.getInfo());
        try
        {
            database.insertOrThrow(MUSIC_TABLE, null, values);
        } catch (SQLiteConstraintException e)
        {
            values.remove(IS_NEW);
            values.put(IS_NEW, OLD);
            database.updateWithOnConflict(MUSIC_TABLE, values, ID + " = ?", new String[]{item.getId()}, SQLiteDatabase.CONFLICT_REPLACE);
        }
        Log.v(TAG, item.toString());
    }

    private void insertDeletedMusicItem(MusicItem item, SQLiteDatabase database)
    {

        ContentValues values = new ContentValues();
        values.put(ID, item.getId());
        values.put(TITLE, item.getTitle());
        values.put(PATH, item.getPath());
        SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
        values.put(TIMESTAMP, currentDate.format(new Date()));
        values.put(IS_NEW, item.getInfo());

        database.updateWithOnConflict(MUSIC_TABLE, values, ID + " = ?", new String[]{item.getId()}, SQLiteDatabase.CONFLICT_REPLACE);

        Log.v(TAG, item.toString());
    }

    public void setAllOld(String table)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + table + " SET " + IS_NEW + " = " + OLD);

        db.close();
    }

    public void deleteItems()
    {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(MUSIC_TABLE, IS_NEW + " = ? ", new String[]{String.valueOf(DELETED)});
        db.close();
    }
}
