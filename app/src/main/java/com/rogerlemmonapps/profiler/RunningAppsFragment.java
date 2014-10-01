package com.rogerlemmonapps.profiler;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by r on 9/29/2014.
 */
public class RunningAppsFragment extends Fragment {
    public static View rootV;
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

        ArrayAdapterItem adapter = new ArrayAdapterItem(this.getActivity(), R.layout.runningappslistitem, runningApps);
        runningAppsList.setAdapter(adapter);
        runningAppsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final RunningApp app = ((RunningApp)view.findViewById(R.id.appName).getTag());
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("Profile name");

                final EditText input = new EditText(getActivity());
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        App.fileUtil.createProfile(app, value);
                    }
                });
                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                alert.show();
            }
        });
    }


    class ArrayAdapterItem extends ArrayAdapter<RunningApp> {

        Context mContext;
        int layoutResourceId;
        RunningApp data[] = null;

        public ArrayAdapterItem(Context mContext, int layoutResourceId, RunningApp[] data) {

            super(mContext, layoutResourceId, data);

            this.layoutResourceId = layoutResourceId;
            this.mContext = mContext;
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null){
                // inflate the layout
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                convertView = inflater.inflate(layoutResourceId, parent, false);
            }

            // object item based on the position
            RunningApp objectItem = data[position];

            // get the TextView and then set the text (item name) and tag (item ID) values
            TextView textViewItem = (TextView) convertView.findViewById(R.id.appName);
            textViewItem.setText(objectItem.appName);
            textViewItem.setTag(objectItem);

            ImageView imageView = (ImageView)convertView.findViewById(R.id.appIcon);
            if(objectItem.icon == null){

            }else {
                imageView.setImageDrawable(objectItem.icon);
            }
            return convertView;

        }

    }
}
