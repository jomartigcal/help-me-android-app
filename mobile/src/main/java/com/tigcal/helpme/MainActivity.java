package com.tigcal.helpme;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
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
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceDetectionApi;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;

public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static final String CONTACT_NUMBER = "com.tigcal.helpme.contact_mobile_number";

    private static final String TAG = "MainActivity";

    private static final int SELECT_CONTACT = 0;
    private static final int HELP_ME = 1;

    private boolean requestingLocationUpdate = true;
    private boolean sendMessages = false;

    private SharedPreferences mPreferences;
    private EditText mContactNumberText;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastKnownLocation;
    private BroadcastReceiver mReceiver;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);

        buildGoogleApiClient();

        mContactNumberText = (EditText) findViewById(R.id.text_contact_number);
        mContactNumberText.setText(mPreferences.getString(CONTACT_NUMBER, ""));

        ImageView saveContactNumber = (ImageView) findViewById(R.id.button_save);
        saveContactNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveContactNumber(mContactNumberText.getText().toString());
            }
        });

        ImageView selectContact = (ImageView) findViewById(R.id.button_get_contact);
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

        Button safeButton = (Button) findViewById(R.id.button_safe);
        safeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopLocationUpdates();
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

//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("turn off");
//        mReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if("turn off".equals(intent.getAction())) {
//                    displayMessage("turn off");
//                    int notificationId = getIntent().getIntExtra(NotificationActivity.NOTIFICATION_EXTRA, 0);
//                    NotificationManagerCompat.from(MainActivity.this).cancel(notificationId);
//                    stopLocationUpdates();
//                }
//            }
//        };
//        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();

        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected() && requestingLocationUpdate) {
            startLocationUpdates();
        }
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void displayNotification() {
        Intent sendMessageIntent = new Intent(this, SmsSendingReceiver.class);
        PendingIntent sendMessagePendingIntent = PendingIntent.getBroadcast(this, 0, sendMessageIntent, 0);

        Intent configureIntent = new Intent(this, MainActivity.class);
        configureIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, configureIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action configureAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_configure, "Configure", pendingIntent)
                .build();
        NotificationCompat.Action wearableConfigureAction = new NotificationCompat.Action.Builder(
                R.drawable.ic_configure, "Configure on Phone", pendingIntent)
                .build();

//        Intent turnOffIntent = new Intent(this, Receiver.class);//"turn off");//new Intent(this, MainActivity.class);
////        turnOffIntent.putExtra(NotificationActivity.NOTIFICATION_EXTRA, HELP_ME);
////        configureIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent turnOfPendingIntent = PendingIntent.getActivity(this, 0, turnOffIntent, 0);//PendingIntent.FLAG_UPDATE_CURRENT);

//        PendingIntent pi = PendingIntent.getBroadcast(this, 0,turnOffIntent, 0);

//        NotificationCompat.Action turnOffAction = new NotificationCompat.Action.Builder(
//                R.drawable.ic_turn_off, "Turn Off", pi)
//                .build();

        Notification notification = new NotificationCompat.Builder(this)
                .setContentText(getString(R.string.label_ask_help))
                .setContentTitle(getString(R.string.app_name))
                .setSmallIcon(R.drawable.ic_notif_alert)
                .setContentIntent(sendMessagePendingIntent)
                .setOngoing(true)
//                .setDeleteIntent()
                .addAction(configureAction)
//                .addAction(turnOffAction)
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
        sendMessages = true;
        requestingLocationUpdate = true;

        String contactNumber = mContactNumberText.getText().toString();
        if (contactNumber == null || "".equals(contactNumber)) {
            displayInvalidNumberMessage();
        } else {
            sendHelpMessage(contactNumber);
        }
    }

    private void sendHelpMessage(String contactNumber) {
        sendMessages = true;
        startService(new Intent(this, SendSmsService.class));
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected(): Successfully connected to Google API client");
        mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (mPreferences != null) {
            saveLocation(mLastKnownLocation);
        }

        createLocationRequest();
        if (requestingLocationUpdate) {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdates() {
        NotificationManagerCompat.from(this).cancel(HELP_ME);

        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        sendMessages = false;
        requestingLocationUpdate = false;

        Intent intent = new Intent(this, SendSmsService.class);
        intent.putExtra(SendSmsService.MESSAGE, getString(R.string.message_safe));
        startService(intent);
        //TODO check
        displayMessage(getString(R.string.message_safe_acknowledgement));
    }

    private void saveLocation(Location location) {
        final StringBuilder nearbyLocationBuilder = new StringBuilder();

        PendingResult<PlaceLikelihoodBuffer> result =
                Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer placeLikelihoods) {
                for (PlaceLikelihood placeLikelihood : placeLikelihoods) {
                    Log.d("test", placeLikelihood.getPlace().getName() + ""
                                    + placeLikelihood.getLikelihood()
                    );
                }

                if (placeLikelihoods.getCount() > 1) {
                    nearbyLocationBuilder.append(placeLikelihoods.get(0).getPlace().getName());
                } else if (placeLikelihoods.getCount() > 2)
                    nearbyLocationBuilder.append(placeLikelihoods.get(0).getPlace().getName() + " and "
                            + placeLikelihoods.get(1).getPlace().getName());


                mPreferences.edit()
                        .putString(SendSmsService.LOCATION_NEARBY, nearbyLocationBuilder.toString())
                        .commit();
                placeLikelihoods.release();
            }
        });

        mPreferences.edit()
                .putLong(SendSmsService.LOCATION_LATITUDE, Double.doubleToLongBits(location.getLatitude()))
                .putLong(SendSmsService.LOCATION_LONGITUDE, Double.doubleToLongBits(location.getLongitude()))
                .commit();

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended(): Connection to Google API client was suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed(): Failed to connect, with result: " + connectionResult);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastKnownLocation = location;
        saveLocation(location);
        startService(new Intent(this, SendSmsService.class));
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(15000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public static class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationManagerCompat.from(context).cancel(HELP_ME);
            //stopLocationUpdates();
        }
    }
}
