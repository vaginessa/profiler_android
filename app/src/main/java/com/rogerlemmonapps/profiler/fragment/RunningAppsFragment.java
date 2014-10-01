package com.rogerlemmonapps.profiler.fragment;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.rogerlemmonapps.profiler.App;
import com.rogerlemmonapps.profiler.R;
import com.rogerlemmonapps.profiler.constant.Constants;
import com.rogerlemmonapps.profiler.data.CreateProfile;
import com.rogerlemmonapps.profiler.data.RunningApp;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by r on 9/29/2014.
 */
public class RunningAppsFragment extends Fragment {
    public static View rootV;
    static TextView profileName;
    static CheckBox forceClose;
    static CheckBox launchApp;

    public RunningAppsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootV = inflater.inflate(R.layout.runningapps, container, false);

        return rootV;
    }

    @Override
    public void onStart() {
        super.onStart();
        final ActivityManager activityManager = (ActivityManager) this.getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningTaskInfo> recentTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);
        ListView runningAppsList = (ListView)rootV.findViewById(R.id.runningAppsList);
        RunningApp[] runningApps = new RunningApp[recentTasks.size()];
        for (int i = 0; i < recentTasks.size(); i++)
        {
            RunningApp app = new RunningApp();
            ActivityManager.RunningTaskInfo runn = recentTasks.get(i);
            app.topActivity = runn.baseActivity != null ? runn.baseActivity.toShortString() : "";
            app.appAddress = app.topActivity.substring(1,app.topActivity.indexOf("/"));
            PackageManager pm = this.getActivity().getPackageManager();
            ApplicationInfo appInfo = null;
            try {
                ComponentName comName= runn.topActivity;
                String packageName = comName.getPackageName();
                appInfo = pm.getApplicationInfo(packageName, 0);
                app.appName = (String) pm.getApplicationLabel(appInfo);
                app.icon = pm.getApplicationIcon(appInfo);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            runningApps[i] = app;
        }

        RunningAppsArrayAdapter adapter = new RunningAppsArrayAdapter(this.getActivity(), R.layout.runningappslistitem, runningApps);
        runningAppsList.setAdapter(adapter);
        runningAppsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> adapterView, View view, int i, long l) {
                final RunningApp app = ((RunningApp) view.findViewById(R.id.appName).getTag());
                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.create_profile_dialog);
                dialog.setTitle(String.format("Create (%s)", app.appName));

                final ListView foldersList = (ListView)dialog.findViewById(R.id.file_folders);
                final FoldersToUseArrayAdapter adapter = new FoldersToUseArrayAdapter(getActivity(), R.layout.filefolderslistitem, app.getApplicationFolders());
                foldersList.setAdapter(adapter);

                final Button create = (Button)dialog.findViewById(R.id.create_profile_button);
                Button cancel = (Button)dialog.findViewById(R.id.cancel_profile_button);

                profileName = (TextView)dialog.findViewById(R.id.profile_name);
                launchApp = (CheckBox)dialog.findViewById(R.id.launch_app);
                forceClose = (CheckBox)dialog.findViewById(R.id.force_close);

                create.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        List<String> folders = new ArrayList<String>();
                        CreateProfile createProfile = new CreateProfile();
                        createProfile.profileName = profileName.getText().toString();
                        for (int i = 0; i < adapter.getCount(); i ++){
                            CheckBox check = ((CheckBox)adapter.getView(i,null,null).findViewById(R.id.use_folder));
                            if(check.isChecked())
                                folders.add(check.getTag().toString());
                        }
                        createProfile.foldersToCopy = folders;
                        createProfile.forceCloseApp = forceClose.isChecked();
                        createProfile.launchApp = launchApp.isChecked();
                        App.fileUtil.createProfile(app, createProfile);
                    }
                });
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                    }
                });
                dialog.show();
            }
        });
    }


    class RunningAppsArrayAdapter extends ArrayAdapter<RunningApp> {
        Context mContext;
        int layoutResourceId;
        RunningApp data[] = null;

        public RunningAppsArrayAdapter(Context mContext, int layoutResourceId, RunningApp[] data) {

            super(mContext, layoutResourceId, data);

            this.layoutResourceId = layoutResourceId;
            this.mContext = mContext;
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                convertView = inflater.inflate(layoutResourceId, parent, false);
            }
            if((position % 2) != 0){
                ((LinearLayout)convertView.findViewById(R.id.runningapps_list_item)).setBackgroundResource(R.color.listOne);
            }else{
                ((LinearLayout)convertView.findViewById(R.id.runningapps_list_item)).setBackgroundResource(R.color.main);
            }


            // object item based on the position
            RunningApp objectItem = data[position];

            // get the TextView and then set the text (item name) and tag (item ID) values
            TextView textViewItem = (TextView) convertView.findViewById(R.id.appName);
            textViewItem.setText(objectItem.appName);
            textViewItem.setTag(objectItem);

            ImageView imageView = (ImageView)convertView.findViewById(R.id.appIcon);
            if(objectItem.icon != null){
                imageView.setImageDrawable(objectItem.icon);
            }
            return convertView;
        }
    }

    class FoldersToUseArrayAdapter extends ArrayAdapter<String> {
        Context mContext;
        int layoutResourceId;
        String data[] = null;

        public FoldersToUseArrayAdapter(Context mContext, int layoutResourceId, String[] data) {
            super(mContext, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.mContext = mContext;
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                convertView = inflater.inflate(layoutResourceId, parent, false);
            }
            CheckBox checkbox = (CheckBox) convertView.findViewById(R.id.use_folder);
            checkbox.setText(data[position]);
            checkbox.setTag(Constants.BASE_APPS_DIR + data[position]);
            checkbox.setChecked(true);
            return convertView;
        }
    }
}
