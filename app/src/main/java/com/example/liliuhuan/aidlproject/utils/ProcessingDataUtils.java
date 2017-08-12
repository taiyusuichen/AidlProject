package com.example.liliuhuan.aidlproject.utils;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.example.liliuhuan.aidlproject.AidlCallback;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by liliuhuan on 2017/8/12.
 */

public class ProcessingDataUtils {
    public RemoteCallbackList<AidlCallback> mRemoteCallbackList;

    public ProcessingDataUtils() {
        mRemoteCallbackList = new RemoteCallbackList<>();
    }


    public void registerCallback(AidlCallback callBack) {
        mRemoteCallbackList.register(callBack);
    }

    public void unRegisterCallback(AidlCallback callBack) {
        mRemoteCallbackList.unregister(callBack);
    }

    public void startDownloading() {
            new AsyncTask<Void, Integer, File>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    int size = mRemoteCallbackList.beginBroadcast();
                    for(int i = 0; i < size; i++){
                        try {
                            mRemoteCallbackList.getBroadcastItem(i).onPreparedStart();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    mRemoteCallbackList.finishBroadcast();
                }

                @Override
                protected void onPostExecute(File result) {
                    super.onPostExecute(result);
                    int size = mRemoteCallbackList.beginBroadcast();
                    for(int i = 0; i < size; i++){
                        try {
                            mRemoteCallbackList.getBroadcastItem(i).onDownloadCompletion();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                    mRemoteCallbackList.finishBroadcast();
                }


                @Override
                protected void onProgressUpdate(Integer... values) {
                    super.onProgressUpdate(values);
                }

                @Override
                protected File doInBackground(Void... params) {
                    try {
                        if (Environment.getExternalStorageState().equals(
                                Environment.MEDIA_MOUNTED)) {
                            URL url = new URL("http://tupian.enterdesk.com/2013/mxy/10/12/5/1.jpg");
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setConnectTimeout(10000);
                            InputStream is = conn.getInputStream();

                            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath(), "123.jpg");
                            FileOutputStream fos = new FileOutputStream(file);
                            BufferedInputStream bis = new BufferedInputStream(is);

                            byte[] buffer = new byte[1024];
                            int len;
                            long total = 0;
                            while ((len = bis.read(buffer)) != -1) {
                                fos.write(buffer, 0, len);
                                total += len;
                                int size = mRemoteCallbackList.beginBroadcast();
                                for(int i = 0; i < size; i++){
                                    try {
                                        mRemoteCallbackList.getBroadcastItem(i).onDownloadProgress(total,conn.getContentLength());
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }
                                mRemoteCallbackList.finishBroadcast();
                            }
                            fos.close();
                            bis.close();
                            is.close();
                            return file;
                        } else {
                            return null;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }.execute();

    }
}
