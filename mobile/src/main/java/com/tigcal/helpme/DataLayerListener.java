package com.tigcal.helpme;

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class DataLayerListener extends WearableListenerService {
    private static final String WEARABLE_MESSAGE = "/start/MainActivity";

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        if (WEARABLE_MESSAGE.equals(messageEvent.getPath())) {
            startService(new Intent(this, SendSmsService.class));
        }
    }
}
