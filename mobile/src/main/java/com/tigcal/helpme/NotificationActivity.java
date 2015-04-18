package com.tigcal.helpme;

import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


public class NotificationActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!getIntent().hasExtra(MainActivity.NOTIFICATION_EXTRA)){
            return;
        }

        int notificationId = getIntent().getIntExtra(MainActivity.NOTIFICATION_EXTRA, 0);

        NotificationManagerCompat.from(this).cancel(notificationId);
        finish();
    }

}
