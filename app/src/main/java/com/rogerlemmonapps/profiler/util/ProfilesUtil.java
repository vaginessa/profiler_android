package com.rogerlemmonapps.profiler.util;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.rogerlemmonapps.profiler.App;
import com.rogerlemmonapps.profiler.constant.Constants;
import com.rogerlemmonapps.profiler.data.Profile;

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

                    File file = new File(profileDirectory, "package");
                    List<String> answer = new ArrayList<String>();
                    try {
                        List<String> comm = new ArrayList<String>();
                        comm.add("while read line; do echo \"$line\"; done < "+ file.getAbsolutePath());
                        answer = ShellUtil.RunAsRoot(comm);
                    }catch (SecurityException ee){
                        ee.printStackTrace();
                    }
                    p.appComponent = answer.get(0);

                    try {
                        appInfo = pm.getApplicationInfo(p.appComponent, 0);
                        p.appName = (String) pm.getApplicationLabel(appInfo);
                        p.icon = pm.getApplicationIcon(appInfo);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    file = new File(profileDirectory, "profile");
                    answer = new ArrayList<String>();
                    try {
                        List<String> comm = new ArrayList<String>();
                        comm.add("while read line; do echo \"$line\"; done < "+ file.getAbsolutePath());
                        answer = ShellUtil.RunAsRoot(comm);
                    }catch (SecurityException ee){
                        ee.printStackTrace();
                    }
                    p.profileName = answer.get(0);
                    profiles.add(p);
                }
            }
        }
        return profiles;
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
}
