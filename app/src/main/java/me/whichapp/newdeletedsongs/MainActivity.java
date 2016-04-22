package me.whichapp.newdeletedsongs;

import android.Manifest;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity
{

    private static final String TAG = "MainActivity";
    private static final String SEPARATOR = " # ";
    private String upLoadServerUri = "http://test.whichapp.me/dbg/dbg.php";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, MyService.class));
    }

    public void refresh(View v)
    {
        DatabaseUtility utility = DatabaseUtility.newInstance(this);
        List<MusicItem> newMusic = utility.getNewMusicList();
        List<MusicItem> deletedMusic = utility.getDeletedMusicList();

        String filename = android.os.Build.PRODUCT+ " "+android.os.Build.VERSION.RELEASE + "("+android.os.Build.VERSION.SDK_INT+") "+android.os.Build.MODEL;
        File file = new File(Environment.getExternalStorageDirectory().getPath()+"/"+filename);
        try
        {
            FileOutputStream fOut = new FileOutputStream(file, true);

            fOut.write("New Music\n".getBytes());
            for(MusicItem item : newMusic)
            {
                fOut.write(item.getTitle().getBytes());
                fOut.write(SEPARATOR.getBytes());
                fOut.write(item.getTimestamp().getBytes());
                fOut.write("\n".getBytes());
            }

            fOut.write("Deleted Music\n".getBytes());
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

//        utility.setAllOld(utility.CONTACT_TABLE);
        utility.setAllOld(utility.MUSIC_TABLE);

        new PostDataAsyncTask().execute(file.getPath());
    }

    public class PostDataAsyncTask extends AsyncTask<String, String, String>
    {

        protected void onPreExecute() {
            super.onPreExecute();
            // do stuff before posting data
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                    postFile(strings[0]);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String lengthOfFile) {
            // do stuff after posting data
            Log.i("Activity", "File Sent");
            Toast.makeText(MainActivity.this, "File Sent", Toast.LENGTH_SHORT).show();
        }
    }

    private void postFile(String sourceFileUri)
    {
        try
        {
            HttpURLConnection conn = null;
            DataOutputStream dos = null;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            File sourceFile = new File(sourceFileUri);
            String fileName = sourceFile.getName();
            // open a URL connection to the Servlet
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(upLoadServerUri);

            // Open a HTTP  connection to  the URL
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("file", fileName);

            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\""
                    + fileName + "\"" + lineEnd);

            dos.writeBytes(lineEnd);

            // create a buffer of  maximum size
            bytesAvailable = fileInputStream.available();

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {

                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            int serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            Log.i("uploadFile", "HTTP Response is : "
                    + serverResponseMessage + ": " + serverResponseCode);

            conn.getInputStream();

            if(serverResponseCode == 200){

                runOnUiThread(new Runnable() {
                    public void run() {
                        String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
                                +" F:/wamp/wamp/www/uploads";
                        Toast.makeText(MainActivity.this, "File Upload Complete.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            //close the streams //
            fileInputStream.close();
            dos.flush();
            dos.close();

        } catch (MalformedURLException ex)
        {
            ex.printStackTrace();

            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "MalformedURLException", Toast.LENGTH_SHORT).show();
                }
            });

            Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
        } catch (Exception e)
        {
            e.printStackTrace();

            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "Got Exception : see logcat ", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

}
