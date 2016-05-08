package me.whichapp.newdeletedsongs;

import android.app.Service;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyService extends Service
{

    private static final int MUSIC_LOADER = 0;
    private static final String ACTION_UPDATE_DB = "update_db";
    private String[] musicProjection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA};
    private String musicSelection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " +
            MediaStore.Audio.Media.DATA + " NOT LIKE ?";
    private String[] musicSelectionArgs = new String[]{"%Notes%"};



    private static final String TAG = "Custom_Service";

    private CursorLoader musicLoader;



    Loader.OnLoadCompleteListener<Cursor> musicListener = new Loader.OnLoadCompleteListener<Cursor>()
    {
        @Override
        public void onLoadComplete(Loader<Cursor> loader, Cursor data)
        {
            Log.i(TAG, "Contact Load finished");

            DatabaseUtility utility = DatabaseUtility.newInstance(MyService.this);
            
            Cursor systemDb = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,new String[] {MediaStore.Audio.Media._ID}, null, null, null);
            if(systemDb != null)
                if(utility.dbItemCount() == systemDb.getCount())
                {
                    Log.d(TAG, "Operation Skipped");
                    systemDb.close();
                    return;
                }

            List<MusicItem> list = utility.getMusicListFromCursor(data);

            Intent intent = new Intent(ACTION_UPDATE_DB, null, MyService.this, UpdateDbIntentService.class);
            intent.putParcelableArrayListExtra("data", (ArrayList<? extends Parcelable>) list);
            startService(intent);
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
        musicLoader = getMusicLoader();
        musicLoader.registerListener(MUSIC_LOADER, musicListener);

        musicLoader.startLoading();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
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
