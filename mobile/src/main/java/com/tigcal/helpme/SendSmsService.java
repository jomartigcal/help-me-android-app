package com.tigcal.helpme;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SendSmsService extends IntentService {
    public static final String LOCATION_LATITUDE = "com.tigcal.helpme.location.latitude";
    public static final String LOCATION_LONGITUDE = "com.tigcal.helpme.location.longitude";
    public static final String LOCATION_NEARBY = "com.tigcal.helpme.location.nearby";

    public static final String MESSAGE = "com.tigcal.helpme.sms";

    public SendSmsService() {
        super("SendSmsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            SharedPreferences preferences = getApplicationContext().getSharedPreferences(
                    getString(R.string.app_name), MODE_PRIVATE);
            String contactNumber = preferences.getString(MainActivity.CONTACT_NUMBER, null);
            double latitude = Double.longBitsToDouble(preferences.getLong(LOCATION_LATITUDE, 0));
            double longitude = Double.longBitsToDouble(preferences.getLong(LOCATION_LONGITUDE, 0));
            String nearbyLocation = preferences.getString(LOCATION_NEARBY, "");

            if (intent.hasExtra(MESSAGE)) {
                sendMessage(contactNumber, intent.getStringExtra(MESSAGE));
            } else {
                sendHelpRequestMessage(contactNumber, latitude, longitude, nearbyLocation);
            }
        }
    }

    private void sendMessage(String contactNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(contactNumber, null, message, null, null);
    }

    private void sendHelpRequestMessage(String contactNumber, double latitude, double longitude, String nearbyLocation) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append(getString(R.string.message_help_me));
        messageBuilder.append(" ");

        if (latitude != 0 && longitude != 0) {
            messageBuilder.append(String.format(getString(R.string.message_location), String.valueOf(latitude), String.valueOf(longitude)));
        }

        if(!"".equals(nearbyLocation)) {
            messageBuilder.append(" (near ");
            messageBuilder.append(nearbyLocation);
            messageBuilder.append(")");
        }

        if (contactNumber != null) {
            sendMessage(contactNumber, messageBuilder.toString());
            displaySmsSentMessage(contactNumber);
        }
    }

    private void displaySmsSentMessage(final String contactNumber) {
        //TODO check
        Handler handler = new Handler(getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "You have asked for help from " + contactNumber, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
