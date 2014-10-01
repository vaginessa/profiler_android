package com.rogerlemmonapps.profiler;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by r on 9/28/2014.
 */

public class FileUtil {

    private static boolean lock;

    public FileUtil(){
        init();
    }
    public static void init(){
        File baseProfilesDirectory = new File(Constants.BASE_PROFILES_DIR);
        if(!baseProfilesDirectory.exists())
            baseProfilesDirectory.mkdir();
    }

    public void switchToProfile(String applicationComponent, int profileNum){
        try {
            if(!lock){
                delete(Constants.BASE_APPS_DIR + applicationComponent + "/", false);
                copyApplicationDataFolder(applicationComponent, profileNum, false);
            }
        }catch(Exception e){

        }
    }

    public void createProfile(RunningApp app){
        createProfile(app, null);
    }

    public void createProfile(RunningApp app, String profileName){
        try {
            String applicationName = app.appAddress;
            int lastProfileNum = ProfilesUtil.findLastProfileNum(applicationName);
            int profileNumber = lastProfileNum + 1;
            copyApplicationDataFolder(applicationName, profileNumber, true);

            //create settings directory
            File createProfilerSettings = new File(Constants.BASE_PROFILES_DIR + "/" + applicationName + "." + profileNumber + "/.profiler");
            try {
                List<String> f = new ArrayList<String>();
                f.add("mkdir " + createProfilerSettings);
                f.add("chmod 777 " + createProfilerSettings);
                ShellUtil.RunAsRoot(f.toArray(new String[1]));
            }catch (SecurityException ee){
                ee.printStackTrace();
            }
            //create package file
            File packageFile = new File(createProfilerSettings.getPath(), "package");
            try {
                List<String> f = new ArrayList<String>();
                f.add("echo \"" + applicationName + "\" > " + packageFile);
                f.add("chmod 777 " + packageFile);
                ShellUtil.RunAsRoot(f.toArray(new String[2]));
            }catch (SecurityException ee){
                ee.printStackTrace();
            }

            //create name file
            File nameFile = new File(createProfilerSettings.getPath(), "profile");
            try {
                List<String> f = new ArrayList<String>();
                String center = profileName!= null ? profileName : profileNumber + "";
                String command = String.format("echo \"%s\" > %s", center, nameFile);
                f.add(command);
                f.add("chmod 777 " + nameFile);
                ShellUtil.RunAsRoot(f.toArray(new String[2]));
            }catch (SecurityException ee){
                ee.printStackTrace();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static void copyApplicationDataFolder(String applicationName, int number, boolean createProfile)
            throws IOException {
        copyApplicationDataFolder(applicationName, number, createProfile, false, "");
    }

    private static void copyApplicationDataFolder(String applicationName, int number, boolean createProfile, boolean recurse, String filename)
            throws IOException {
        if (!lock || recurse) {
            lock = true;
            File sourceLocation;
            File targetLocation;
            if (createProfile) {
                sourceLocation = new File(Constants.BASE_APPS_DIR + applicationName + filename);
                targetLocation = new File(Constants.BASE_PROFILES_DIR + applicationName + "." + number + filename );
            } else {
                sourceLocation = new File(Constants.BASE_PROFILES_DIR + applicationName + "." + number + filename);
                targetLocation = new File(Constants.BASE_APPS_DIR + applicationName + filename);
            }

            boolean sourceIsDir = false;
            try {
                List<String> f = new ArrayList<String>();
                f.add("[ -d  \"" + sourceLocation + "\" ]&&echo \"exists\"");
                sourceIsDir = ShellUtil.RunAsRoot(f.toArray(new String[1])).length > 0;
            }catch (SecurityException ee){
                ee.printStackTrace();
            }
            if (sourceIsDir) {
                if (!targetLocation.exists()) {
                    try {
                        List<String> f = new ArrayList<String>();
                        f.add("mkdir " + targetLocation);
                        f.add("chmod 771 " + targetLocation);
                        ShellUtil.RunAsRoot(f.toArray(new String[2]));
                    }catch (SecurityException ee){
                        ee.printStackTrace();
                    }
                }
                String[] files = null;
                try {
                    List<String> f = new ArrayList<String>();
                    f.add("cd " + sourceLocation);
                    f.add("ls");
                    files = ShellUtil.RunAsRoot(f.toArray(new String[2]));
                }catch (SecurityException ee){
                    ee.printStackTrace();
                }
                if(files != null) {
                    int amt = files.length;
                    for (int i = 0; i < amt; i++) {
                        String file = files[i];
                        if(filename != null){
                            file = filename + "/" + files[i];
                        }
                        copyApplicationDataFolder(applicationName, number, createProfile, true, file);
                    }
                }
            } else {
                try {
                    Log.d("copying", sourceLocation.getAbsolutePath());
                    List<String> f = new ArrayList<String>();
                    f.add("cp -RP " + sourceLocation + " " + targetLocation);
                    f.add("chmod 771 " + targetLocation);
                    ShellUtil.RunAsRoot(f.toArray(new String[2]));
                }catch (SecurityException ee){
                    ee.printStackTrace();
                }
            }
        }
        lock = false;
    }

    public boolean delete(String location, boolean deleteBase){
        try {
            Log.d("deleting", location);
            List<String> f = new ArrayList<String>();
            if(!deleteBase) {
                //f.add("cd " + location);
                f.add("rm -rf " + location + "*");
            }else {
                f.add("rm -rf " + location);
            }
            ShellUtil.RunAsRoot(f.toArray(new String[f.size()]));
            return true;
        }catch (SecurityException ee){
            ee.printStackTrace();
        }
        return false;
    }

}
