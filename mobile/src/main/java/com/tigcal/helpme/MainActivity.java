package com.tigcal.helpme;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "MainActivity";
    private static final String SEND_MESSAGE = "com.tigcal.helpme.send_message";
    private static final String CONTACT_NUMBER = "com.tigcal.helpme.contact_mobile_number";
    private static final int SELECT_CONTACT = 0;
    private static final int HELP_ME = 1;

    private SharedPreferences mPreferences;
    private EditText mContactNumberText;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastKnownLocation;
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);

        buildGoogleApiClient();

        mContactNumberText = (EditText) findViewById(R.id.text_contact_number);
        mContactNumberText.setText(mPreferences.getString(CONTACT_NUMBER, ""));

        Button saveContactNumber = (Button) findViewById(R.id.button_save);
        saveContactNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveContactNumber(mContactNumberText.getText().toString());
            }
        });

        Button selectContact = (Button) findViewById(R.id.button_get_contact);
        selectContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, SELECT_CONTACT);
                }
            }
        });

        ImageView askHelpImage = (ImageView) findViewById(R.id.image_ask_help);
        askHelpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askForHelp();
            }
        });

        displayNotification();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            new AboutDialog(this).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_CONTACT && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
            Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                mContactNumberText.setText(cursor.getString(index));
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SEND_MESSAGE);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (SEND_MESSAGE.equals(intent.getAction())) {
                    sendHelpMessage(mContactNumberText.getText().toString());
                }
            }
        };
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();

        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void displayNotification() {
        Intent sendMessageIntent = new Intent(SEND_MESSAGE);
        PendingIntent sendMessagePendingIntent = PendingIntent.getBroadcast(this, 0, sendMessageIntent, 0);

        Intent configureIntent = new Intent(this, MainActivity.class);
        configureIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, configureIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action configureAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_notif_alert, "Configure", pendingIntent)
                .build();
        NotificationCompat.Action wearableConfigureAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_notif_alert, "Configure on Phone", pendingIntent)
                .build();

        Intent turnOffIntent = new Intent(this, NotificationActivity.class);
        turnOffIntent.putExtra(NotificationActivity.NOTIFICATION_EXTRA, HELP_ME);
        configureIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent turnOfPendingIntent = PendingIntent.getActivity(this, 0, turnOffIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action turnOffAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_notif_alert, "Turn Off", turnOfPendingIntent)
                .build();

        Notification notification = new NotificationCompat.Builder(this)
                .setContentText(getString(R.string.label_ask_help))
                .setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_notif_alert)
                .setContentIntent(sendMessagePendingIntent)
//                .setOngoing(true)
                .addAction(configureAction)
                .addAction(turnOffAction)
                .extend(new NotificationCompat.WearableExtender()
                                .addAction(wearableConfigureAction)
                )
                .build();
        NotificationManagerCompat.from(this).notify(HELP_ME, notification);
    }

    private void saveContactNumber(String contactNumber) {
        if (contactNumber == null || "".equals(contactNumber)) {
            displayInvalidNumberMessage();
            return;
        }

        mPreferences.edit()
                .putString(CONTACT_NUMBER, contactNumber)
                .commit();
        displayMessage("The mobile number " + contactNumber + " has been saved.");

    }

    private void displayInvalidNumberMessage() {
        displayMessage(getString(R.string.message_invalid_contact_number));
    }

    private void displayMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void askForHelp() {
        String contactNumber = mContactNumberText.getText().toString();
        if (contactNumber == null || "".equals(contactNumber)) {
            displayInvalidNumberMessage();
        } else {
            sendHelpMessage(contactNumber);
        }
    }

    private void sendHelpMessage(String contactNumber) {
        Intent intent = new Intent(this, SendSmsService.class);
        intent.putExtra(SendSmsService.MOBILE_NUMBER, contactNumber);
        if (mLastKnownLocation != null) {
            intent.putExtra(SendSmsService.LOCATION_LATITUDE, mLastKnownLocation.getLatitude());
            intent.putExtra(SendSmsService.LOCATION_LONGITUDE, mLastKnownLocation.getLongitude());
        }
        startService(intent);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected(): Successfully connected to Google API client");
        mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended(): Connection to Google API client was suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed(): Failed to connect, with result: " + connectionResult);
    }

}
