package com.tigcal.helpme;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.telephony.SmsManager;
import android.widget.Toast;

public class SendSmsService extends IntentService {
    public static final String LOCATION_LATITUDE = "com.tigcal.helpme.location.latitude";
    public static final String LOCATION_LONGITUDE = "com.tigcal.helpme.location.longitude";
//    public static final String MOBILE_NUMBER = "com.tigcal.helpme.mobilenumber";

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

            sendMessage(contactNumber, latitude, longitude);
        }
    }

    private void sendMessage(String contactNumber, double latitude, double longitude) {
        //TODO invalid number or none set yet
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Please help me! I am in an emergency! ");//TODO strings.xml

        if (latitude != 0 && longitude != 0) {
            //TODO check comparison
            messageBuilder.append("My last location is near the following GPS coordinates: ");//TODO strings.xml
            messageBuilder.append(String.valueOf(latitude));
            messageBuilder.append(",");
            messageBuilder.append(String.valueOf(longitude));
        }

        if (contactNumber != null) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(contactNumber, null, messageBuilder.toString(), null, null);
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
