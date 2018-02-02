package com.gg.musicdot;

/**
 * Created by Administrator on 2017/11/22 0022.
 */

public interface UCallBack {
    void onSuccess(String content, int code);
    void onFail(String error);
}
