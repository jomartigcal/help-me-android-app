package com.tigcal.helpme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SmsSendingReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, SendSmsService.class));
    }
}
