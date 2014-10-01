package com.rogerlemmonapps.profiler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.os.Bundle;
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
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by r on 9/29/2014.
 */
public class ProfilesFragment extends Fragment {
    public static View rootV;
    public ArrayAdapterItem adapter;

    public ProfilesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootV = inflater.inflate(R.layout.profiles, container, false);

        return rootV;
    }

    @Override
    public void onStart() {
        super.onStart();
        Profile[] profilesList = null;
        ListView profilesListView = (ListView) rootV.findViewById(R.id.profileslist);
        adapter = new ArrayAdapterItem(this.getActivity(), R.layout.profileslistitem, ProfilesUtil.getAllProfiles());
        profilesListView.setAdapter(adapter);
        profilesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Profile profile = ((Profile) view.findViewById(R.id.profileAppName).getTag());
                App.fileUtil.switchToProfile(profile.appComponent, Integer.parseInt(profile.profileNumber));
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
                App.fileUtil.delete(Constants.BASE_PROFILES_DIR + profileToUse.appComponent + "." + profileToUse.profileNumber, true );
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
