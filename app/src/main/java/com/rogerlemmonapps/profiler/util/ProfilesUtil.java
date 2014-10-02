package com.rogerlemmonapps.profiler.util;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.JsonReader;

import com.rogerlemmonapps.profiler.App;
import com.rogerlemmonapps.profiler.constant.Constants;
import com.rogerlemmonapps.profiler.data.CreateProfile;
import com.rogerlemmonapps.profiler.data.Profile;
import com.rogerlemmonapps.profiler.data.RunningApp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by r on 9/29/2014.
 */

public class ProfilesUtil {

    public static int findLastProfileNum(String applicationName){
        int lastProfileNumber = 0;
        File f = new File(Constants.BASE_PROFILES_DIR);
        File[] files = f.listFiles();
        if(files != null) {
            for (File inFile : files) {
                String name = inFile.getName();
                if (name.contains(applicationName) && inFile.isDirectory()) {
                    int number = Integer.parseInt(name.substring(name.lastIndexOf('.') + 1, name.length()));
                    if(number > lastProfileNumber)
                        lastProfileNumber = number;
                }
            }
        }
        return lastProfileNumber;
    }

    public static List<Profile> getAllProfiles() {
        List<Profile> profiles = new ArrayList<Profile>();
        File f = new File(Constants.BASE_PROFILES_DIR);
        File[] files = f.listFiles();
        if (files != null) {
            for (File inFile : files) {
                if (inFile.isDirectory()) {
                    File profileDirectory = new File(inFile.getPath() + "/.profiler/");
                    Profile p = new Profile();
                    p.profileNumber = inFile.getPath().substring(inFile.getPath().lastIndexOf('.') + 1, inFile.getPath().length());
                    PackageManager pm = App.app.getApplicationContext().getPackageManager();
                    ApplicationInfo appInfo = null;

                    File file = new File(profileDirectory, "settings");
                    List<String> answer = new ArrayList<String>();
                    try {
                        List<String> comm = new ArrayList<String>();
                        comm.add("while read line; do echo \"$line\"; done < "+ file.getAbsolutePath());
                        answer = ShellUtil.RunAsRoot(comm);
                    }catch (SecurityException ee){
                        ee.printStackTrace();
                    }
                    JSONObject settingsJson = null;
                    try {
                        settingsJson = new JSONObject(answer.get(0).replace("\'", "\'"));
                        p.profileName = settingsJson.getString("profileName");
                        p.appComponent = settingsJson.getString("appAddress");
                        p.forceClose = settingsJson.getBoolean("forceClose");
                        p.launchApp = settingsJson.getBoolean("launchApp");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        appInfo = pm.getApplicationInfo(p.appComponent, 0);
                        p.appName = (String) pm.getApplicationLabel(appInfo);
                        p.icon = pm.getApplicationIcon(appInfo);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    profiles.add(p);
                }
            }
        }
        return profiles;
    }

    public static Profile getProfileByAddress(String profileAddress){
        Profile profile = new Profile();
        File f = new File(Constants.BASE_PROFILES_DIR + profileAddress);
        File[] files = f.listFiles();
        if (files != null) {
            for (File inFile : files) {
                if (inFile.isDirectory()) {
                    File profileDirectory = new File(inFile.getPath() + "/.profiler/");
                    profile.profileNumber = inFile.getPath().substring(inFile.getPath().lastIndexOf('.') + 1, inFile.getPath().length());
                    PackageManager pm = App.app.getApplicationContext().getPackageManager();
                    ApplicationInfo appInfo = null;

                    File file = new File(profileDirectory, "settings");
                    List<String> answer = new ArrayList<String>();
                    try {
                        List<String> comm = new ArrayList<String>();
                        comm.add("while read line; do echo \"$line\"; done < "+ file.getAbsolutePath());
                        answer = ShellUtil.RunAsRoot(comm);
                    }catch (SecurityException ee){
                        ee.printStackTrace();
                    }
                    JSONObject settingsJson = null;
                    try {
                        settingsJson = new JSONObject(answer.get(0).replace("\'", "\'"));
                        profile.profileName = settingsJson.getString("profileName");
                        profile.appComponent = settingsJson.getString("appAddress");
                        profile.forceClose = settingsJson.getBoolean("forceClose");
                        profile.launchApp = settingsJson.getBoolean("launchApp");
                       // profile.appTopActivity = settingsJson.getString("topActivity");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    try {
                        appInfo = pm.getApplicationInfo(profile.appComponent, 0);
                        profile.appName = (String) pm.getApplicationLabel(appInfo);
                        profile.icon = pm.getApplicationIcon(appInfo);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return profile;
    }

    public static void renameProfile(Profile profileToUse, String value) {
        try {
            int profileNumber = Integer.parseInt(profileToUse.profileNumber);
            if(value == null){
                value = profileNumber + "";
            }
            //create name file
            try{
                File createProfilerSettings = new File(Constants.BASE_PROFILES_DIR + "/" + profileToUse.appComponent+ "." + profileNumber + "/.profiler");
                File nameFile = new File(createProfilerSettings.getPath(), "profile");
                FileWriter nameWriter = new FileWriter(nameFile);
                nameWriter.write(value);
                nameWriter.flush();
                nameWriter.close();
            }catch(IOException ee){
                ee.printStackTrace();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void createProfile(RunningApp app, CreateProfile createProfile){
        try {
            String applicationName = app.appAddress;
            int lastProfileNum = ProfilesUtil.findLastProfileNum(applicationName);
            int profileNumber = lastProfileNum + 1;
            new FileUtil().copyApplicationDataFolder(applicationName, profileNumber, createProfile);

            //create settings directory
            File createProfilerSettings = new File(Constants.BASE_PROFILES_DIR + "/" + applicationName + "." + profileNumber + "/.profiler");
            try {
                List<String> f = new ArrayList<String>();
                f.add("mkdir " + createProfilerSettings);
                f.add("chmod 771 " + createProfilerSettings);
                ShellUtil.RunAsRoot(f);
            }catch (SecurityException ee){
                ee.printStackTrace();
            }

            //create settings files
            JSONObject settingsJson = new JSONObject();
            settingsJson.put("profileName", createProfile.profileName.length() > 0 ? createProfile.profileName : profileNumber + "");
            settingsJson.put("profileNumber", profileNumber);
            settingsJson.put("appAddress", app.appAddress);
            settingsJson.put("launchApp", createProfile.launchApp);
            settingsJson.put("forceClose", createProfile.forceCloseApp);

            File settingsFile = new File(createProfilerSettings.getPath(), "settings");
            try {
                List<String> f = new ArrayList<String>();
                f.add("echo \"" + settingsJson.toString().replace("\"", "\'") + "\" > " + settingsFile);
                f.add("chmod 777 " + settingsFile);
                ShellUtil.RunAsRoot(f);
            }catch (SecurityException ee){
                ee.printStackTrace();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void switchToProfile(Profile profile){
        try {
            if(!profile.lock){
                FileUtil fileUtil = new FileUtil(profile);
                fileUtil.deleteApplicationFolders(Constants.BASE_APPS_DIR + profile.appComponent + "/", false);
                fileUtil.copyApplicationDataFolder(profile.appComponent, Integer.parseInt(profile.profileNumber), null);
                fileUtil.launchApplication(profile);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void createShortcut(Profile profile){
        Intent shortcutIntent = new Intent();
        shortcutIntent.setClassName(Constants.PROFILER_PACKAGE, Constants.SHORTCUTS_ENTRY);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, profile.profileName);
        try {
            PackageManager pm = App.app.getPackageManager();
            Intent launchIntent = pm.getLaunchIntentForPackage(profile.appComponent);
            String className = launchIntent.getComponent().getClassName();
            Drawable drawable = pm.getActivityInfo(new ComponentName(profile.appComponent, className), 0).loadIcon(pm) ;
            Bitmap b = ((BitmapDrawable)drawable).getBitmap() ;
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, b) ;

            ApplicationInfo appInfo = pm.getApplicationInfo(profile.appComponent, 0);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, appInfo.icon);
            addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            App.app.sendBroadcast(addIntent);
        }catch(PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
    }

}
