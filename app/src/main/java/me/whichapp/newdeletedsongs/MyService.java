package me.whichapp.newdeletedsongs;

import android.app.Service;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.util.List;

public class MyService extends Service
{

    private static final int MUSIC_LOADER = 0;
    private String[] musicProjection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA};
    private String musicSelection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " +
            MediaStore.Audio.Media.DATA + " NOT LIKE ?";
    private String[] musicSelectionArgs = new String[]{"%Notes%"};


    private static final String TAG = "Custom_Service";
    private String[] contactProjection = {ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
    private static final int CONTACT_LOADER = 1;

    private CursorLoader musicLoader;
    private CursorLoader contactLoader;

    Loader.OnLoadCompleteListener<Cursor> contactListener = new Loader.OnLoadCompleteListener<Cursor>()
    {
        @Override
        public void onLoadComplete(Loader<Cursor> loader, Cursor data)
        {
            Log.i(TAG, "Contact Load finished");

            DatabaseUtility utility = DatabaseUtility.newInstance(MyService.this);
            List<ContactItem> list = utility.getContactListFromCursor(data);
            if (list != null)
            {
                utility.insertContactList(list);
            }
        }
    };

    Loader.OnLoadCompleteListener<Cursor> musicListener = new Loader.OnLoadCompleteListener<Cursor>()
    {
        @Override
        public void onLoadComplete(Loader<Cursor> loader, Cursor data)
        {
            Log.i(TAG, "Contact Load finished");

            DatabaseUtility utility = DatabaseUtility.newInstance(MyService.this);
            List<MusicItem> list = utility.getMusicListFromCursor(data);
            if (list != null)
            {
                utility.insertMusicList(list);
            }
        }
    };

    public MyService()
    {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i(TAG, "Service started");
        return Service.START_STICKY;
    }

    private CursorLoader getContactLoader()
    {
        Log.d(TAG, "Contact Loader Created");
        return new CursorLoader(
                this,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                contactProjection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

    }

    private CursorLoader getMusicLoader()
    {
        Log.d(TAG, "Music Loader Created");
        return new CursorLoader(
                this,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                musicProjection,
                musicSelection,
                musicSelectionArgs,
                MediaStore.Audio.Media.TITLE);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
//        contactLoader = getContactLoader();
//        contactLoader.registerListener(MUSIC_LOADER, contactListener);

        musicLoader = getMusicLoader();
        musicLoader.registerListener(CONTACT_LOADER, musicListener);

//        contactLoader.startLoading();
        musicLoader.startLoading();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
//        if (contactLoader != null)
//        {
//            contactLoader.unregisterListener(contactListener);
//            contactLoader.cancelLoad();
//            contactLoader.stopLoading();
//        }
        if (musicLoader != null)
        {
            musicLoader.unregisterListener(musicListener);
            musicLoader.cancelLoad();
            musicLoader.stopLoading();
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

}
