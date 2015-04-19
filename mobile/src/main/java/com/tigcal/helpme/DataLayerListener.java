package com.tigcal.helpme;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class DataLayerListener extends WearableListenerService {
    private static final String WEARABLE_MESSAGE = "/start/MainActivity";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        Log.d("test", "wear message:" + messageEvent.getPath());
        if (WEARABLE_MESSAGE.equals(messageEvent.getPath())) {

            Intent startIntent = new Intent(this, MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);

            startService(new Intent(this, SendSmsService.class));
        }
    }
}
