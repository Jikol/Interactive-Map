package com.example.interactivemap;

import android.app.Application;

public class Context extends Application {

    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    public static void setContext(Context mContext) {
        Context.mContext = mContext;
    }
}
