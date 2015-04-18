package com.tigcal.helpme;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class HelpMeWearActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_me_wear);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                ImageView askHelpImage = (ImageView) findViewById(R.id.image_ask_help);
                askHelpImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        askForHelp();
                    }
                });

            }
        });
    }

    private void askForHelp() {
        Toast.makeText(this, "message", Toast.LENGTH_SHORT).show();
    }
}
