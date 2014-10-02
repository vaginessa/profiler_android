package com.rogerlemmonapps.profiler.util;

import android.content.Intent;
import android.util.Log;

import com.rogerlemmonapps.profiler.App;
import com.rogerlemmonapps.profiler.constant.Constants;
import com.rogerlemmonapps.profiler.data.CreateProfile;
import com.rogerlemmonapps.profiler.data.Profile;
import com.rogerlemmonapps.profiler.data.RunningApp;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by r on 9/28/2014.
 */

public class FileUtil {
    public static Profile profile;

    public FileUtil(){
        this.profile = new Profile();
        init();
    }

    public FileUtil(Profile profile){
        this.profile = profile;
        init();

    }
    public static void init(){
        File baseProfilesDirectory = new File(Constants.BASE_PROFILES_DIR);
        if(!baseProfilesDirectory.exists())
            baseProfilesDirectory.mkdir();
    }

    public void launchApplication(Profile profile) {
        String thisShouldBeThePID = "0";
        List<String> answer = null;

        if(profile.forceClose) {
            //get the apps pid from shell
            try {
                List<String> comm = new ArrayList<String>();
                comm.add("ps | grep '" + profile.appComponent + "'");
                answer = ShellUtil.RunAsRoot(comm);
                //this is hacky as shit
                if (answer.size() > 0) {
                    //split by 3 spaces and take the second result
                    String[] temp = answer.get(0).split("   ");
                    //take the first number till the first space
                    thisShouldBeThePID = temp[1].substring(0, temp[1].indexOf(" "));
                }
            } catch (SecurityException ee) {
                ee.printStackTrace();
            }

            //kill the app with sudo
            try {
                if (Integer.parseInt(thisShouldBeThePID) > 0) {
                    try {
                        List<String> comm = new ArrayList<String>();
                        comm.add("kill " + thisShouldBeThePID);
                        ShellUtil.RunAsRoot(comm);
                    } catch (SecurityException ee) {
                        ee.printStackTrace();
                    }
                }
            } catch (NumberFormatException ex) {
                Log.e("Profiler", thisShouldBeThePID + " is not a valid integer");
            }
        }

        if(profile.launchApp) {
            //run the app
            Intent LaunchIntent = App.app.getPackageManager().getLaunchIntentForPackage(profile.appComponent);
            LaunchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            App.app.startActivity(LaunchIntent);
        }
    }

    public static void copyApplicationDataFolder(String applicationName, int number, CreateProfile createProfile)
            throws IOException {
        copyApplicationDataFolder(applicationName, number, createProfile, false, "");
    }

    private static void copyApplicationDataFolder(String applicationName, int number, CreateProfile createProfile, boolean recurse, String filename)
            throws IOException {
        if (!profile.lock || recurse) {
            profile.lock = true;
            File sourceLocation;
            File targetLocation;
            if (createProfile != null) {
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
            boolean secondTest = createProfile != null ? (createProfile.foldersToCopy.contains(sourceLocation) || filename.length() == 0) : true;
            if (sourceIsDir && secondTest) {
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
            }
        }
        profile.lock = false;
    }

    public boolean deleteApplicationFolders(String location, boolean deleteBase){
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

    static List<String> folders;
    static String baseFolder;
    public static List<String> getApplicationFileFolders(String applicationAddress, String filename){
        if(baseFolder == null)
            baseFolder = applicationAddress;
        String start = filename != null && filename.length() > 0 ? filename : "";
        applicationAddress = applicationAddress + start;
        if(folders == null){
            folders = new ArrayList<String>();
        }
        boolean sourceIsDir = false;
        try {
            List<String> f = new ArrayList<String>();
            f.add("[ -d  \"" + applicationAddress + "\" ]&&echo \"exists\"");
            sourceIsDir = ShellUtil.RunAsRoot(f).size() > 0;
        }catch (SecurityException ee){
            ee.printStackTrace();
        }
        if (sourceIsDir) {
            folders.add(applicationAddress.replace(baseFolder, ""));
            List<String> files = null;
            try {
                List<String> f = new ArrayList<String>();
                f.add("cd " + applicationAddress);
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
                    getApplicationFileFolders(applicationAddress, file);
                }
            }
        }
        return folders;
    }
}
