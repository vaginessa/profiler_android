package com.rogerlemmonapps.profiler.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rogerlemmonapps.profiler.App;
import com.rogerlemmonapps.profiler.constant.Constants;
import com.rogerlemmonapps.profiler.data.Profile;
import com.rogerlemmonapps.profiler.R;
import com.rogerlemmonapps.profiler.util.ProfilesUtil;

import java.util.List;

/**
 * Created by r on 9/29/2014.
 */
public class ProfilesFragment extends Fragment {
    public static View rootV;
    public ArrayAdapterItem adapter;
    public List<Profile> profiles = null;
    public ProgressBar progress;

    public ProfilesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootV = inflater.inflate(R.layout.profiles, container, false);
        return rootV;
    }

    private class GetAllProfiles extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                progress.setVisibility(View.VISIBLE);
                progress.setIndeterminate(true);
                profiles = null;
                profiles = ProfilesUtil.getAllProfiles();
                while (profiles == null) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }catch(Exception e){
                Log.e("Profiler", "View gone");
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                finishStart();

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        progress = (ProgressBar)this.getActivity().findViewById(R.id.progress);
        try {
            new GetAllProfiles().execute("");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void finishStart(){
        ListView profilesListView = (ListView) rootV.findViewById(R.id.profileslist);
        adapter = new ArrayAdapterItem(this.getActivity(), R.layout.profileslistitem, profiles);
        profilesListView.setAdapter(adapter);
        profilesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Profile profile = ((Profile) view.findViewById(R.id.profileAppName).getTag());
                ProfilesUtil.switchToProfile(profile);
            }
        });

        profilesListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                AdapterView.AdapterContextMenuInfo info;
                try {
                    info = (AdapterView.AdapterContextMenuInfo) menuInfo;
                } catch (ClassCastException e) {
                    return;
                }

                Profile profile = (Profile) adapter.getItem(info.position);
                if (profile == null) {
                    return;
                }

                menu.setHeaderTitle(profile.appName + ":" + profile.profileName);
                menu.add(Menu.NONE, Constants.MENU_CONTEXT_RENAME_PROFILE, Menu.NONE, "Rename Profile");
                menu.add(Menu.NONE, Constants.MENU_CONTEXT_DELETE_PROFILE, Menu.NONE, "Delete Profile");
            }

        });

        progress.setVisibility(View.GONE);
        progress.setIndeterminate(false);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            return false;
        }
        final Profile profileToUse = adapter.getItem(info.position);
        switch (item.getItemId()) {
            case Constants.MENU_CONTEXT_DELETE_PROFILE: {
                adapter.remove(profileToUse);
                App.fileUtil.deleteApplicationFolders(Constants.BASE_PROFILES_DIR + profileToUse.appComponent + "." + profileToUse.profileNumber, true);
                return true;
            }
            case Constants.MENU_CONTEXT_RENAME_PROFILE: {
                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                alert.setTitle("Edit profile name");

                final EditText input = new EditText(getActivity());
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        profileToUse.profileName = value;
                        ProfilesUtil.renameProfile(profileToUse, value);
                        adapter.notifyDataSetChanged();
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

                alert.show();
            }
        }
        return false;
    }
    class ArrayAdapterItem extends ArrayAdapter<Profile> {

        Context mContext;
        int layoutResourceId;
        List<Profile> data = null;

        public ArrayAdapterItem(Context mContext, int layoutResourceId, List<Profile> data) {
            super(mContext, layoutResourceId, data);
            this.layoutResourceId = layoutResourceId;
            this.mContext = mContext;
            this.data = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                convertView = inflater.inflate(layoutResourceId, parent, false);
            }
            if((position % 2) != 0){
                ((LinearLayout)convertView.findViewById(R.id.profiles_list_item)).setBackgroundResource(R.color.listOne);
            }else{
                ((LinearLayout)convertView.findViewById(R.id.profiles_list_item)).setBackgroundResource(R.color.main);
            }
            Profile objectItem = data.get(position);

            TextView textViewItem = (TextView) convertView.findViewById(R.id.profileAppName);
            textViewItem.setText(objectItem.appName);
            textViewItem.setTag(objectItem);

            TextView textViewItem2 = (TextView) convertView.findViewById(R.id.profileName);
            textViewItem2.setText(objectItem.profileName);

            ImageView imageView = (ImageView) convertView.findViewById(R.id.profileAppIcon);
            if (objectItem.icon == null) {

            } else {
                imageView.setImageDrawable(objectItem.icon);
            }
            return convertView;
        }
    }
}
