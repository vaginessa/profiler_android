package com.rogerlemmonapps.profiler;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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

    public void switchToProfile(Profile profile){
        try {
            if(!lock){
                delete(Constants.BASE_APPS_DIR + profile.appComponent + "/", false);
                copyApplicationDataFolder(profile.appComponent, Integer.parseInt(profile.profileNumber), false);
                launchApplication(profile);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void launchApplication(Profile profile) {
        String thisShouldBeThePID = "0";
        List<String> answer = null;

        //get the apps pid from shell
        try {
            List<String> comm = new ArrayList<String>();
            comm.add("ps | grep '" + profile.appComponent + "'");
            answer = ShellUtil.RunAsRoot(comm);
            //this is hacky as shit
            if(answer.size() > 0) {
                //split by 3 spaces and take the second result
                String[] temp = answer.get(0).split("   ");
                //take the first number till the first space
                thisShouldBeThePID = temp[1].substring(0, temp[1].indexOf(" "));
            }
        }catch (SecurityException ee){
            ee.printStackTrace();
        }

        //kill the app with sudo
        try {
            if(Integer.parseInt(thisShouldBeThePID) > 0) {
                try {
                    List<String> comm = new ArrayList<String>();
                    comm.add("kill " + thisShouldBeThePID);
                    ShellUtil.RunAsRoot(comm);
                } catch (SecurityException ee) {
                    ee.printStackTrace();
                }
            }
        }catch (NumberFormatException ex){
            Log.e("Profiler", thisShouldBeThePID + " is not a valid integer");
        }

        //run the app
        Intent LaunchIntent = App.app.getPackageManager().getLaunchIntentForPackage(profile.appComponent);
        LaunchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP  | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        App.app.startActivity(LaunchIntent);
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
                f.add("chmod 771 " + createProfilerSettings);
                ShellUtil.RunAsRoot(f);
            }catch (SecurityException ee){
                ee.printStackTrace();
            }
            //create package file
            File packageFile = new File(createProfilerSettings.getPath(), "package");
            try {
                List<String> f = new ArrayList<String>();
                f.add("echo \"" + applicationName + "\" > " + packageFile);
                f.add("chmod 771 " + packageFile);
                ShellUtil.RunAsRoot(f);
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
                f.add("chmod 771 " + nameFile);
                ShellUtil.RunAsRoot(f);
            }catch (SecurityException ee){
                ee.printStackTrace();
            }

            //create pid file
            /*File pidFile = new File(createProfilerSettings.getPath(), "pid");
            try {
                List<String> f = new ArrayList<String>();
                ActivityManager activityManager = (ActivityManager) App.app
                        .getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> pidsTask = activityManager.getRunningAppProcesses();
                int thisShouldBeThePID = 0;
                for(int i = 0; i < pidsTask.size(); i++){
                    if(pidsTask.get(i).processName.contains(app.appAddress)){
                        thisShouldBeThePID = pidsTask.get(i).uid;
                    }
                }
                String center = thisShouldBeThePID + "";
                String command = String.format("echo \"%s\" > %s", center, pidFile);
                f.add(command);
                f.add("chmod 771 " + pidFile);
                ShellUtil.RunAsRoot(f);
            }catch (SecurityException ee){
                ee.printStackTrace();
            }*/

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
                sourceIsDir = ShellUtil.RunAsRoot(f).size() > 0;
            }catch (SecurityException ee){
                ee.printStackTrace();
            }
            if (sourceIsDir) {
                if (!targetLocation.exists()) {
                    //copy the whole directory -RPa to recurse, preserve perm and ownership
                    try {
                        List<String> f = new ArrayList<String>();
                        f.add("cp -RPa " + sourceLocation + " " + targetLocation);
                        ShellUtil.RunAsRoot(f);
                    }catch (SecurityException ee){
                        ee.printStackTrace();
                    }
                }
                List<String> files = null;
                try {
                    List<String> f = new ArrayList<String>();
                    f.add("cd " + sourceLocation);
                    f.add("ls");
                    files = ShellUtil.RunAsRoot(f);
                }catch (SecurityException ee){
                    ee.printStackTrace();
                }
                if(files != null) {
                    int amt = files.size();
                    for (int i = 0; i < amt; i++) {
                        String file = files.get(i);
                        if(filename != null){
                            file = filename + "/" + file;
                        }
                        copyApplicationDataFolder(applicationName, number, createProfile, true, file);
                    }
                }
            } else {
                //no longer copying files like this
                /*try {
                    Log.d("copying", sourceLocation.getAbsolutePath());
                    List<String> f = new ArrayList<String>();
                    f.add("cp -RP " + sourceLocation + " " + targetLocation);
                    f.add("chmod 771 " + targetLocation);
                    ShellUtil.RunAsRoot(f);
                }catch (SecurityException ee){
                    ee.printStackTrace();
                }*/
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
            ShellUtil.RunAsRoot(f);
            return true;
        }catch (SecurityException ee){
            ee.printStackTrace();
        }
        return false;
    }

}
