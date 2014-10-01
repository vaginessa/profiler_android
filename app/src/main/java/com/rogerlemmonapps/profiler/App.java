package com.rogerlemmonapps.profiler;

import android.app.Application;

import com.rogerlemmonapps.profiler.util.FileUtil;

import java.io.IOException;

public class App extends Application {

    public static App app;
    public static FileUtil fileUtil;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        fileUtil = new FileUtil();
        final Runtime runtime = Runtime.getRuntime();
        try {
            runtime.exec("su");
        } catch (IOException e) {
            String a = "";
        }
    }
}