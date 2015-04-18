package com.tigcal.helpme;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;


public class WearActivity extends Activity {

    private TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);
        mTextView = (TextView) findViewById(R.id.text);
    }
}