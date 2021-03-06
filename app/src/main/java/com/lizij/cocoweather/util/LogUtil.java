package com.lizij.cocoweather.util;

import android.util.Log;

import com.lizij.cocoweather.application.AppApplication;

/**
 * Created by Lizij on 2017/6/29.
 */

public class LogUtil{
    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;

    public static int level = Integer.parseInt(AppApplication.getProperties().getProperty("LOG_LEVEL"));

    public static void v(String tag, String msg){
        if (level <= VERBOSE){
            Log.v(tag, tag + ":" + msg);
        }
    }

    public static void d(String tag, String msg){
        if (level <= DEBUG){
            Log.d(tag, tag + ":" + msg);
        }
    }

    public static void i(String tag, String msg){
        if (level <= INFO){
            Log.i(tag, tag + ":" + msg);
        }
    }

    public static void w(String tag, String msg){
        if (level <= WARN){
            Log.w(tag, tag + ":" + msg);
        }
    }

    public static void e(String tag, String msg){
        if (level <= ERROR){
            Log.e(tag, tag + ":" + msg);
        }
    }
}
