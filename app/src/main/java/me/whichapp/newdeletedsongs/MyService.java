package me.whichapp.newdeletedsongs;

import android.app.Service;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyService extends Service
{

    private static final int MUSIC_LOADER = 0;
    private String[] musicProjection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA};
    private String musicSelection = MediaStore.Audio.Media.IS_MUSIC + " != 0 AND " +
            MediaStore.Audio.Media.DATA + " NOT LIKE ?";
    private String[] musicSelectionArgs = new String[]{"%Notes%"};

    private static final String SEPARATOR = " # ";
    private String upLoadServerUri = "http://test.whichapp.me/dbg/dbg.php";

    private static final String TAG = "Custom_Service";

    private CursorLoader musicLoader;

    class DbAndFileTask extends AsyncTask<List<MusicItem>, Void, Void>
    {

        @Override
        protected Void doInBackground(List<MusicItem>[] params)
        {
            List<MusicItem> list = params[0];
            DatabaseUtility utility = DatabaseUtility.newInstance(MyService.this);
            if (list != null)
            {
                utility.insertMusicList(list);
            }

            List<MusicItem> newMusic = utility.getNewMusicList();
            List<MusicItem> deletedMusic = utility.getDeletedMusicList();

            String filename = android.os.Build.PRODUCT+ " "+android.os.Build.VERSION.RELEASE + "("+android.os.Build.VERSION.SDK_INT+") "+android.os.Build.MODEL;
            File file = new File(Environment.getExternalStorageDirectory().getPath()+"/"+filename);
            try
            {
                FileOutputStream fOut = new FileOutputStream(file, true);
                fOut.write("\n\n========Load Complete=========\n".getBytes());
                fOut.write("Timestamp : ".getBytes());
                SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH);
                fOut.write(currentDate.format(new Date()).getBytes() );
                fOut.write("\n".getBytes());

                fOut.write("\n#### New Music ####\n".getBytes());
                for(MusicItem item : newMusic)
                {
                    fOut.write(item.getTitle().getBytes());
                    fOut.write(SEPARATOR.getBytes());
                    fOut.write(item.getTimestamp().getBytes());
                    fOut.write("\n".getBytes());
                }

                fOut.write("\n#### Deleted Music ####\n".getBytes());
                for(MusicItem item : deletedMusic)
                {
                    fOut.write(item.getTitle().getBytes());
                    fOut.write(SEPARATOR.getBytes());
                    fOut.write(item.getTimestamp().getBytes());
                    fOut.write("\n".getBytes());
                }
                fOut.flush();
                fOut.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            Log.e(TAG, "New Music List"+newMusic.toString());
            Log.e(TAG, "Deleted Music List"+deletedMusic.toString());

            utility.deleteItems();
            utility.setAllOld(utility.MUSIC_TABLE);

            return null;
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
               new DbAndFileTask().execute(list);
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
