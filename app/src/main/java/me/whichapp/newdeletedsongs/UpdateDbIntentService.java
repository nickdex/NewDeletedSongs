package me.whichapp.newdeletedsongs;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UpdateDbIntentService extends IntentService
{
    private static final String SEPARATOR = " # ";
    private static final String TAG = UpdateDbIntentService.class.getSimpleName();
    private static final String ACTION_UPDATE_DB = "update_db";
    private String upLoadServerUri = "http://test.whichapp.me/dbg/dbg.php";

    public UpdateDbIntentService()
    {
        super("UpdateDbIntentService");
    }



    @Override
    protected void onHandleIntent(Intent intent)
    {
        if (intent != null && intent.getAction().equals(ACTION_UPDATE_DB))
        {
            List<MusicItem> list = intent.getParcelableArrayListExtra("data");
            DatabaseUtility utility = DatabaseUtility.newInstance(this);
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

            this.stopSelf();
        }
    }

}
