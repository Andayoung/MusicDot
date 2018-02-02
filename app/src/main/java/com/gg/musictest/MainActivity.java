package com.gg.musictest;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


import com.gg.musicdot.PlayMusicPresenter;
import com.gg.musicdot.UUView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity{
    MediaPlayer mp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mp=new MediaPlayer();
        PlayMusicPresenter.initMusic("123","123");
        PlayMusicPresenter playMusicPresenter = new PlayMusicPresenter();
        playMusicPresenter.getMusic(MainActivity.this, "稻香", new UUView() {
            @Override
            public void showContent(String content, int code) {
                mp.reset();
                try {
                    mp.setDataSource(content);
                    mp.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mp.start();
            }

            @Override
            public void showError(String error) {

            }
        });
    }
}
