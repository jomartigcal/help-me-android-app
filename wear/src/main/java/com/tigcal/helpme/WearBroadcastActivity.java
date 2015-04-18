package com.tigcal.helpme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


public class WearBroadcastActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = new Intent();
        i.setAction("com.tigcal.helpme.SHOW_NOTIFICATION");
        i.putExtra(WearNotificationReceiver.CONTENT_KEY, getString(R.string.app_name));
        sendBroadcast(i);
        finish();
    }
}
