package com.rogerlemmonapps.profiler.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rogerlemmonapps.profiler.R;

import java.util.List;

/**
 * Created by r on 9/29/2014.
 */
public class ToolsFragment extends Fragment {
    public static View rootV;
    public ProgressBar progress;

    public ToolsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootV = inflater.inflate(R.layout.tools, container, false);
        return rootV;
    }

    private class GetAllProfiles extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            finishStart();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        try {
            new GetAllProfiles().execute("");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void finishStart(){
    }
}
