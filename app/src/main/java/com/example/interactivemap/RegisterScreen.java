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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class RegisterScreen extends AppCompatActivity {

    private EditText name;
    private EditText password;
    private TextView nameValidate;
    private TextView passwordValidate;
    private TextView nameSize;
    private TextView passwordSize;
    private Spinner focus;

    public Intent intent;
    private Database db;

    private static List<String> spinnerString;
    private static ArrayAdapter<String> spinnerAdapter;

    public static final Integer MAX_NAME_SIZE = 20;
    public static final Integer MIN_NAME_SIZE = 5;
    public static final Integer MAX_PASSWORD_SIZE = 30;
    public static final Integer MIN_PASSWORD_SIZE = 6;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" + "(?=.*[0-9])" + "(?=.*[a-zA-Z])"
                     + "(?=\\S+$)" + ".{6,30}" + "$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);
        getWindow().setNavigationBarColor(getResources().getColor(R.color.dark_blue));

        name = findViewById(R.id.loginScreen_name);
        password = findViewById(R.id.loginScreen_password);
        nameValidate = findViewById(R.id.registerScreen_nameValidate);
        nameSize = findViewById(R.id.registerScreen_nameSize);
        passwordValidate = findViewById(R.id.registerScreen_passwordValidate);
        passwordSize = findViewById(R.id.registerScreen_passwordSize);

        focus = findViewById(R.id.registerScreen_focus);
        String[] values = {"Soldier", "Commander", "Engineer"};
        spinnerString = new ArrayList<>(Arrays.asList(values));
        spinnerAdapter = new ArrayAdapter<>(this, R.layout.register_spinner_select, spinnerString);
        spinnerAdapter.setDropDownViewResource(R.layout.register_spinner);
        focus.setAdapter(spinnerAdapter);

        addKeyListener();
    }

    private boolean validateName() {
        String nameInput = name.getText().toString().trim();

        if (nameInput.isEmpty()) {
            nameValidate.setText("Name can not be empty");
            return false;
        } else if (nameInput.length() < MIN_NAME_SIZE || nameInput.length() > MAX_NAME_SIZE) {
            nameValidate.setText("Name must have proper length");
            return false;
        } else {
            nameValidate.setText(null);
            return true;
        }
    }

    private boolean validatePassword() {
        String passwordInput = password.getText().toString().trim();

        if (passwordInput.isEmpty()) {
            passwordValidate.setText("Password can not be empty");
            return false;
        } else if (!PASSWORD_PATTERN.matcher(passwordInput).matches()) {
            passwordValidate.setText("Password is too weak");
            return false;
        } else {
            passwordValidate.setText(null);
            return true;
        }
    }

    public void confirmInput(View v) {
        if (!validateName() | !validatePassword()) {
            return;
        }

        db = new Database(this);
        final boolean[] isInserted = {false};
        Member member = new Member();

        new AsyncTask() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            protected void onPreExecute() {
                member.setName(name.getText().toString());
                String hashedPassword = Member.generatePassword(password.getText().toString());
                member.setPassword(hashedPassword);
                member.setFocus(focus.getSelectedItem().toString());
            }
            @Override
            protected Object doInBackground(Object[] objects) {
                return db.insertMember(member);
            }
            @Override
            protected void onPostExecute(Object o) {
                isInserted[0] = (boolean)o;

                if (isInserted[0]) {
                    login(member);
                } else {
                    Toast.makeText(getApplicationContext(), "Something gone wrong", Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    public void login(Member member) {
        final boolean[] isLogged = {false};

        new AsyncTask() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            protected void onPreExecute() { }
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
                    do {
                        member.setName(cursor.getString(cursor.getColumnIndex("name")));
                        member.setFocus(cursor.getString(cursor.getColumnIndex("focus")));
                        member.setId(Integer.parseInt(cursor.getString(cursor.getColumnIndex("member_id"))));
                    } while (cursor.moveToNext());
                    cursor.close();
                    isLogged[0] = true;
                }

                if (isLogged[0]) {
                    intent = new Intent(RegisterScreen.this, MapScreen.class);
                    intent.putExtra("auth_result", isLogged[0]);
                    Gson gson = new Gson();
                    String jsonMember = gson.toJson(member);
                    intent.putExtra("user", jsonMember);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Something gone wrong", Toast.LENGTH_LONG).show();
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
                nameSize.setText(charSequence.toString().length() + "/" + MAX_NAME_SIZE);
                if (charSequence.toString().length() >= MAX_NAME_SIZE) {
                    name.getText().delete(charSequence.toString().length() - 1, charSequence.toString().length());
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                passwordSize.setText(charSequence.toString().length() + "/" + MAX_PASSWORD_SIZE);
                if (charSequence.toString().length() >= MAX_NAME_SIZE) {
                    password.getText().delete(charSequence.toString().length() - 1, charSequence.toString().length());
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
}