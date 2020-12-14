package com.example.interactivemap;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class LoginScreen extends AppCompatActivity {

    private EditText name;
    private EditText password;
    private Button login;

    private Intent intent;
    private Database db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_screen);
        getWindow().setNavigationBarColor(getResources().getColor(R.color.dark_blue));

        name = findViewById(R.id.loginScreen_name);
        password = findViewById(R.id.loginScreen_password);
        login = findViewById(R.id.loginScreen_login);
        login.setClickable(false);
        login.setAlpha((float)0.5);

        addKeyListener();
    }

    public void login(View v) {
        db = new Database(this);
        final boolean[] isLogged = {false};
        Member member = new Member();

        new AsyncTask() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            protected void onPreExecute() {
                member.setName(name.getText().toString());
            }
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            protected Object doInBackground(Object[] objects) {
                return db.getMember(member);
            }
            @Override
            protected void onPostExecute(Object o) {
                Cursor cursor = (Cursor)o;
                if (cursor.getCount() >= 1) {
                    cursor.moveToFirst();
                    String hashedPassword = cursor.getString(cursor.getColumnIndex("password"));
                    BCrypt.Result result = Member.verifyPassword(password.getText().toString(), hashedPassword);
                    if (result.verified) {
                        do {
                            member.setName(cursor.getString(cursor.getColumnIndex("name")));
                            member.setFocus(cursor.getString(cursor.getColumnIndex("focus")));
                            member.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex("member_id"))));
                        } while (cursor.moveToNext());
                        cursor.close();
                        isLogged[0] = true;
                    }
                }

                if (isLogged[0]) {
                    intent = new Intent(LoginScreen.this, MapScreen.class);
                    intent.putExtra("auth_result", isLogged[0]);
                    Gson gson = new Gson();
                    String jsonMember = gson.toJson(member);
                    intent.putExtra("user", jsonMember);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Your credentials are incorrect", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    public void addKeyListener() {
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().length() > RegisterScreen.MAX_NAME_SIZE) {
                    name.getText().delete(charSequence.toString().length() - 1, charSequence.toString().length());
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                if (name.getText().length() >= RegisterScreen.MIN_NAME_SIZE && name.getText().length() <= RegisterScreen.MAX_NAME_SIZE) {
                    if (password.getText().length() >= RegisterScreen.MIN_PASSWORD_SIZE && name.getText().length() <= RegisterScreen.MAX_PASSWORD_SIZE) {
                        login.setClickable(true);
                        login.setAlpha((float)1);
                    }
                }
            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().length() > RegisterScreen.MAX_PASSWORD_SIZE) {
                    password.getText().delete(charSequence.toString().length() - 1, charSequence.toString().length());
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                if (name.getText().length() >= RegisterScreen.MIN_NAME_SIZE && name.getText().length() <= RegisterScreen.MAX_NAME_SIZE) {
                    if (password.getText().length() >= RegisterScreen.MIN_PASSWORD_SIZE && name.getText().length() <= RegisterScreen.MAX_PASSWORD_SIZE) {
                        login.setClickable(true);
                        login.setAlpha((float)1);
                    }
                }
            }
        });
    }
}