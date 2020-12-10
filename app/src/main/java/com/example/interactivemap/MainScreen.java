package com.example.interactivemap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.mapbox.mapboxsdk.Mapbox;

public class MainScreen extends AppCompatActivity implements View.OnClickListener {

    public Intent intent;

    private Button showFriend;
    private Button newUser;
    private Button loginUser;
    private Button guestMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);

        showFriend = findViewById(R.id.mainScreen_friendIdButton);
        newUser = findViewById(R.id.mainScreen_newUserButton);
        loginUser = findViewById(R.id.mainScreen_loginUserButton);
        guestMap = findViewById(R.id.mainScreen_guestMapButton);

        showFriend.setOnClickListener(this);
        newUser.setOnClickListener(this);
        loginUser.setOnClickListener(this);
        guestMap.setOnClickListener(this);
    }

    private void openMapScreen() {
        intent = new Intent(this, MapScreen.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mainScreen_friendIdButton: {

            } break;
            case R.id.mainScreen_newUserButton: {

            } break;
            case R.id.mainScreen_loginUserButton: {

            } break;
            case R.id.mainScreen_guestMapButton: {
                openMapScreen();
            } break;
        }
    }
}