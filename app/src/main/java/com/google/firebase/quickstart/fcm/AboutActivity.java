package com.google.firebase.quickstart.fcm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.quickstart.fcm.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_about);
    }
}
