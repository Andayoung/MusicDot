package com.gg.musicdot;


import android.content.Context;
import android.os.Build;
import android.util.Log;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2018/1/8 0008.
 */

public class PlayMusicModle {
    private UCallBack callBack;

    public void getResult(Context context, String content, UCallBack uCallBack) {
        callBack = uCallBack;
        searchSong(content);
    }

    private void searchSong(final String musicName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .addHeader("User-Agent", makeUA())
                            .url("http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.search.catalogSug&query=" + musicName)
                            .build();
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        searchSongAdd(getSongId(responseData));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void searchSongAdd(final String songid) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .addHeader("User-Agent", makeUA())
                            .url("http://tingapi.ting.baidu.com/v1/restserver/ting?method=baidu.ting.song.play&songid=" + songid)
                            .build();
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        callBack.onSuccess(getSongAddUrl(responseData), 0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private String getSongId(String jsonData) {
        String id = "";
        id = jsonData.substring(jsonData.indexOf("songid\":\"") + 9, jsonData.indexOf("\",\"has_mv"));
        return id;
    }

    private String getSongAddUrl(String songAdd) {
        String add = "";
        add = songAdd.substring(songAdd.indexOf("show_link\":\"") + 12, songAdd.indexOf("\",\"free"));
        add = add.replace("\\", "");
        return add;
    }

    private String makeUA() {
        final String ua = Build.BRAND + "/" + Build.MODEL + "/" + Build.VERSION.RELEASE;
        return ua;
    }

}