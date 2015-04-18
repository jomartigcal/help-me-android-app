package com.tigcal.helpme;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
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

    private static final String CONTACT_NUMBER = "contact_mobile_number";
    public static final String SEND_MESSAGE = "send_message";
    private static final int SELECT_CONTACT = 0;
    private static final int HELP_ME = 1;

    private SharedPreferences mPreferences;
    private EditText mContactNumberText;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastKnownLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);

        buildGoogleApiClient();

        ImageView askHelpImage = (ImageView) findViewById(R.id.image_ask_help);
        askHelpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                askForHelp();
            }
        });

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
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(getIntent().hasExtra(SEND_MESSAGE)) {
            sendHelpMessage(mContactNumberText.getText().toString());
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
        Intent configureIntent = new Intent(this, MainActivity.class);
//        configureIntent.putExtra(SEND_MESSAGE, true);
        configureIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, configureIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action configureAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_notif_alert, "Configure", pendingIntent)
                .build();

        Notification notification = new NotificationCompat.Builder(this)
                .setContentText(getString(R.string.label_ask_help))
                .setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_notif_alert)
//                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .addAction(configureAction)
//                .addAction()//TODO close
                .build();
        NotificationManagerCompat.from(this).notify(HELP_ME, notification);
    }

    private void saveContactNumber(String contactNumber) {
        if (contactNumber == null || "".equals(contactNumber)) {
            displayInvalidNumberMessage();
            return;
        }

        mPreferences.edit()
                .putString("contact_mobile_number", contactNumber)
                .commit();

    }

    private void displayInvalidNumberMessage() {
        Toast.makeText(this, getString(R.string.message_invalid_contact_number), Toast.LENGTH_SHORT).show();
    }

    private void askForHelp() {
        sendHelpMessage(mPreferences.getString(CONTACT_NUMBER, ""));
    }

    private void sendHelpMessage(String contactNumber) {
        //TODO add location
        //TODO check if sent via pending intent

        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Please help me! I am in an emergency! ");//TODO strings.xml

        if (mLastKnownLocation != null) {
            messageBuilder.append("My last location is near the following GPS coordinates: ");//TODO strings.xml
            messageBuilder.append(String.valueOf(mLastKnownLocation.getLatitude()));
            messageBuilder.append(",");
            messageBuilder.append(String.valueOf(mLastKnownLocation.getLongitude()));
        }

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(contactNumber, null, messageBuilder.toString(), null, null);
        Toast.makeText(this, "You have asked for help from " + contactNumber, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(getString(R.string.app_name), "onConnectionSuspended:" + i);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(getString(R.string.app_name), "onConnectionFailed");
    }

}
