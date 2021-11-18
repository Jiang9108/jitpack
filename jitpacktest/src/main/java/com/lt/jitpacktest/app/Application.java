package com.lt.jitpacktest.app;

import android.content.Context;


public class Application extends android.app.Application {
    private static Context context;

    public static Context getContext() {
        return context;
    }


    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();

    }


}
