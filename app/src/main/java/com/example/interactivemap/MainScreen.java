package com.example.interactivemap;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
        getWindow().setNavigationBarColor(getResources().getColor(R.color.dark_blue));

        showFriend = findViewById(R.id.mainScreen_friendIdButton);
        newUser = findViewById(R.id.mainScreen_newUserButton);
        loginUser = findViewById(R.id.mainScreen_loginUserButton);
        guestMap = findViewById(R.id.mainScreen_guestMapButton);

        showFriend.setOnClickListener(this);
        newUser.setOnClickListener(this);
        loginUser.setOnClickListener(this);
        guestMap.setOnClickListener(this);

        Context.setContext((Context) getApplicationContext());
    }

    private void openMapScreen() {
        intent = new Intent(this, MapScreen.class);
        startActivity(intent);
    }

    private void openNewUser() {
        intent = new Intent(this, RegisterScreen.class);
        startActivity(intent);
    }

    private void openLogin() {
        intent = new Intent(this, LoginScreen.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mainScreen_friendIdButton: {

            } break;
            case R.id.mainScreen_newUserButton: {
                openNewUser();
            } break;
            case R.id.mainScreen_loginUserButton: {
                openLogin();
            } break;
            case R.id.mainScreen_guestMapButton: {
                openMapScreen();
            } break;
        }
    }
}