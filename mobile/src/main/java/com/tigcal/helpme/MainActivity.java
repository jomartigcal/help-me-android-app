package com.tigcal.helpme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

    private static final String CONTACT_NUMBER = "contact_mobile_number";
    private static final int SELECT_CONTACT = 0;

    private SharedPreferences mPreferences;
    private EditText mContactNumberText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);

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
        if(requestCode == SELECT_CONTACT && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            String[] projection = new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER};
            Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null);
            if(cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                mContactNumberText.setText(cursor.getString(index));
            }
        }
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
        SmsManager smsManager = SmsManager.getDefault();
        //TODO add location
        //TODO check if sent via pending intent

        smsManager.sendTextMessage(contactNumber, null, "Hello! I need help!", null, null);
    }
}
