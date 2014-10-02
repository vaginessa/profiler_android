package com.rogerlemmonapps.profiler.constant;

import android.content.Context;

import com.rogerlemmonapps.profiler.App;

/**
 * Created by r on 9/29/2014.
 */
public class Constants {

    public static final String BASE_APPS_DIR = "/data/data/";
    public static final String PROFILES_DIR = "profiles";
    public static final String BASE_PROFILES_DIR = App.app.getApplicationContext().getDir(Constants.PROFILES_DIR, Context.MODE_PRIVATE).getPath() + "/";
    public static final int MENU_CONTEXT_DELETE_PROFILE = 1;
    public static final int MENU_CONTEXT_RENAME_PROFILE = 2;
    public static final String PROFILER_KEY = "profiler_key";
    public static final String PROFILE_ADDRESS = "profile_address";
    public static final String PROFILER_PACKAGE = "com.rogerlemmonapps.profiler";
    public static final String SHORTCUTS_ENTRY = "ProfileShortcut";
    public static final int MENU_CONTEXT_CREATE_SHORTCUT = 4;
}
