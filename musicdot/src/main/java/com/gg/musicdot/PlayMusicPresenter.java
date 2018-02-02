package com.gg.musicdot;

import android.content.Context;

/**
 * Created by Administrator on 2018/1/8 0008.
 */

public class PlayMusicPresenter {
    private PlayMusicModle uModel;

    public static void initMusic(String userId,String key){
        //验证userkey和key
    }
    public void getMusic(Context context, String content, final UUView uView) {
        uModel = new PlayMusicModle();
        uModel.getResult(context, content, new UCallBack() {
            @Override
            public void onSuccess(String content, int code) {
                uView.showContent(content, code);
            }

            @Override
            public void onFail(String error) {
                uView.showError(error);
            }

        });
    }
}