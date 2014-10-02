package com.rogerlemmonapps.profiler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.rogerlemmonapps.profiler.constant.Constants;
import com.rogerlemmonapps.profiler.data.Profile;
import com.rogerlemmonapps.profiler.util.ProfilesUtil;

/**
 * Created by roger on 10/2/2014.
 */
public class ProfileShortcut extends Activity {

    private Intent intent;
    private Bundle bundle;
    private String profilerSecureKey;
    private String profileAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        runTheShortcut();
    }

    @Override
    protected void onStart() {
        runTheShortcut();
    }

    public void runTheShortcut(){
        intent = getIntent();
        if(intent == null)
            finish();
        bundle = intent.getExtras();
        if(bundle == null)
            finish();
        if(bundle.containsKey(Constants.PROFILER_KEY)){
            profilerSecureKey = bundle.getString(Constants.PROFILER_KEY);
        }
        if(profilerSecureKey == null)
            finish();
        if(bundle.containsKey(Constants.PROFILE_ADDRESS)){
            profileAddress = bundle.getString(Constants.PROFILE_ADDRESS);
        }
        if(profileAddress == null)
            finish();

        Profile profile = ProfilesUtil.getProfileByAddress(profileAddress);
        if(profile == null)
            finish();

        ProfilesUtil.switchToProfile(profile);
        finish();
    }
}
