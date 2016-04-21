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
        db.execSQL("CREATE TABLE IF NOT EXISTS " + CONTACT_TABLE + " ( " + ID + " TEXT PRIMARY KEY, " + TITLE + " TEXT, " + NUMBER + " TEXT, " + TIMESTAMP + " TEXT, " + IS_NEW + " INTEGER);");
        Log.v(TAG, "Database is created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + MUSIC_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + CONTACT_TABLE);
        onCreate(db);
    }

    public void insertContactList(List<ContactItem> fresh)
    {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(CONTACT_TABLE, new String[]{ID, TITLE, NUMBER, IS_NEW}, null, null, null, null, null);

        if (cursor != null)
        {
            if (cursor.getCount() == 0)
            {
                for (ContactItem item : fresh)
                {
                    item.setInfo(NEW);
                    insertContactItem(item, db);
                }
            } else
            {
                List<ContactItem> updatedList = getUpdatedContactItemList(getContactListFromDatabaseCursorToInsert(cursor), fresh);
                if (updatedList != null)
                {
                    for (ContactItem item : updatedList)
                    {
                        insertContactItem(item, db);
                    }
                }
            }
        }

        db.close();
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
                List<MusicItem> updatedList = getUpdatedMusicItemList(getMusicListFromDatabaseCursorToInsert(cursor), fresh);
                if (updatedList != null)
                {
                    for (MusicItem item : updatedList)
                    {
                        insertMusicItem(item, db);
                    }
                }
            }
        }

        db.close();
    }

    public List<ContactItem> getDeletedContactList()
    {
        return getSpecialisedContactList(DELETED);
    }

    public List<MusicItem> getDeletedMusicList()
    {
        return getSpecialisedMusicList(DELETED);
    }

    public List<ContactItem> getNewContactList()
    {
        return getSpecialisedContactList(NEW);
    }

    public List<MusicItem> getNewMusicList()
    {
        return getSpecialisedMusicList(NEW);
    }


    private List<ContactItem> getSpecialisedContactList(int info_type)
    {
        SQLiteDatabase db = getReadableDatabase();
        List<ContactItem> list = new ArrayList<>();
        Cursor cursor = db.query(CONTACT_TABLE, null, IS_NEW + " = " + info_type, null, null, null, TITLE);
        while(cursor.moveToNext())
        {
            String id = cursor.getString(cursor.getColumnIndex(ID));
            String title = cursor.getString(cursor.getColumnIndex(TITLE));
            String number = cursor.getString(cursor.getColumnIndex(NUMBER));
            String timestamp = cursor.getString(cursor.getColumnIndex(TIMESTAMP));
            int info = cursor.getInt(cursor.getColumnIndex(IS_NEW));

            ContactItem item = new ContactItem(id, title, number, timestamp, info);
            list.add(item);
        }
        cursor.close();
        db.close();
        return list;
    }

    private List<MusicItem> getSpecialisedMusicList(int info_type)
    {
        SQLiteDatabase db = getReadableDatabase();
        List<MusicItem> list = new ArrayList<>();
        Cursor cursor = db.query(MUSIC_TABLE, null, IS_NEW + " = " + info_type, null, null, null, TITLE);
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

    public List<ContactItem> getContactItemListFromDatabase()
    {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor;
        cursor = db.query(CONTACT_TABLE, new String[]{ID, TITLE, NUMBER, IS_NEW}, null, null, null, null, null);

        if (cursor != null)
        {
            return getSortedContactList(getContactListFromDatabaseCursorToShow(cursor, db, CONTACT_TABLE));
        }

        db.close();
        return null;
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

    private List<ContactItem> getSortedContactList(List<ContactItem> list)
    {
        List<ContactItem> tempList = new ArrayList<>();
        for (ContactItem item : list)
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

    /**
     * Heart of logic
     *
     * @param old
     * @param fresh
     * @return
     */
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

    private List<ContactItem> getUpdatedContactItemList(List<ContactItem> old, List<ContactItem> fresh)
    {
        List<ContactItem> oldItems = new ArrayList<>();
        List<ContactItem> changedItems = new ArrayList<>();

        //New Item or Changed
        if (fresh.size() >= old.size())
        {
            for (ContactItem itemFresh : fresh)
            {
                for (ContactItem itemOld : old)
                {
                    if (itemFresh.getId().equals(itemOld.getId()))
                    {
                        if (itemFresh.getContent().equals(itemOld.getContent()))
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

            for (ContactItem item : fresh)
            {
                item.setInfo(NEW);
            }
            for (ContactItem item : changedItems)
            {
                item.setInfo(CHANGED);
            }

            fresh.addAll(changedItems);

            return fresh;
        }
        //Deleted Item
        else if (fresh.size() < old.size())
        {
            for (ContactItem itemOld : old)
            {
                for (ContactItem itemFresh : fresh)
                {
                    if (itemFresh.getId().equals(itemOld.getId()))
                    {
                        oldItems.add(itemOld);
                        break;
                    }
                }
            }

            old.removeAll(oldItems);

            for (ContactItem item : old)
            {
                item.setInfo(DELETED);
            }

            return old;
        }

        return null;
    }


    private List<ContactItem> getContactListFromDatabaseCursorToInsert(Cursor cursor)
    {
        if (cursor != null && cursor.getCount() > 0)
        {
            List<ContactItem> list = new ArrayList<>();
            while (cursor.moveToNext())
            {
                String id = cursor.getString(0);
                String title = cursor.getString(1);
                String number = cursor.getString(2);
                int info = cursor.getInt(3);
                ContactItem item = new ContactItem(id, title, number, info);
                list.add(item);
            }

            return list;
        } else
        {
            return null;
        }
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

    private List<ContactItem> getContactListFromDatabaseCursorToShow(Cursor cursor, SQLiteDatabase db, String table)
    {
        if (cursor != null && cursor.getCount() > 0)
        {
            List<ContactItem> list = new ArrayList<>();
            while (cursor.moveToNext())
            {
                String id = cursor.getString(0);
                String title = cursor.getString(1);
                String number = cursor.getString(2);
                int info = cursor.getInt(3);
                ContactItem item = new ContactItem(id, title, number, info);
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

    /**
     * Converts Cursor to equivalent List of ContactItem
     * @param cursor
     * @return List containing ContactItem
     */

    public List<ContactItem> getContactListFromCursor(Cursor cursor)
    {
        if (cursor != null && cursor.getCount() > 0)
        {
            List<ContactItem> list = new ArrayList<>();
            while (cursor.moveToNext())
            {

                String id = cursor.getString(0);
                String title = cursor.getString(1);
                String number = cursor.getString(2);
                //OLD doesn't matter it will be replaced later when interacting with database
                ContactItem item = new ContactItem(id, title, number, OLD);
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

    /**
     * Converts Cursor to equivalent List of ContactItem
     * @param cursor
     * @return List containing ContactItem
     */

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
                MusicItem item = new MusicItem(id, title, path, OLD);
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

    private void insertContactItem(ContactItem item, SQLiteDatabase database)
    {

        ContentValues values = new ContentValues();
        values.put(ID, item.getId());
        values.put(TITLE, item.getContent());
        values.put(NUMBER, item.getNumber());
        values.put(IS_NEW, item.getInfo());
        SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
        values.put(TIMESTAMP, currentDate.format(new Date()));
        try
        {
            database.insertOrThrow(CONTACT_TABLE, null, values);
        } catch (SQLiteConstraintException e)
        {
            database.updateWithOnConflict(CONTACT_TABLE, values, ID + " = ?", new String[]{item.getId()}, SQLiteDatabase.CONFLICT_REPLACE);
        }
        Log.v(TAG, item.toString());
    }

    private void insertMusicItem(MusicItem item, SQLiteDatabase database)
    {

        ContentValues values = new ContentValues();
        values.put(ID, item.getId());
        values.put(TITLE, item.getTitle());
        values.put(PATH, item.getPath());
        values.put(IS_NEW, item.getInfo());
        SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
        values.put(TIMESTAMP, currentDate.format(new Date()));
        try
        {
            database.insertOrThrow(MUSIC_TABLE, null, values);
        } catch (SQLiteConstraintException e)
        {
            database.updateWithOnConflict(MUSIC_TABLE, values, ID + " = ?", new String[]{item.getId()}, SQLiteDatabase.CONFLICT_REPLACE);
        }
        Log.v(TAG, item.toString());
    }

    public void setAllOld(String table)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE " + table + " SET " + IS_NEW + " = " + OLD);

        db.close();
    }
}
