package com.rogerlemmonapps.profiler.data;

import android.graphics.drawable.Drawable;

import com.rogerlemmonapps.profiler.constant.Constants;
import com.rogerlemmonapps.profiler.util.FileUtil;

import java.util.List;

/**
 * Created by r on 9/29/2014.
 */
public class RunningApp{
    public RunningApp(){}
    public Drawable icon;
    public String topActivity;
    public String appAddress;
    public String appName;
    public String[] getApplicationFolders(){
        List<String> list = FileUtil.getApplicationFileFolders(Constants.BASE_APPS_DIR + appAddress, "");
        list.remove(0);
        return list.toArray(new String[list.size()]);
    }
}