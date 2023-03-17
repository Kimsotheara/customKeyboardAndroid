package com.klsoft.custom.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.klsoft.custom.checksum.SkDecoder;
import com.klsoft.custom.keyboard.SecureKeyboard;
import com.klsoft.custom.R;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    SecureKeyboard sc;
    private static final String TAG = "LoginActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sc = findViewById(R.id.secure_keyboard);
        EditText username = findViewById(R.id.username);
        EditText password = findViewById(R.id.password);
        sc.registerTextKeyboard(username, false);
        sc.registerTextKeyboard(password, true);
        final Button loginButton = findViewById(R.id.login);
        HashMap<Character, Drawable> icons = new HashMap<>();
        icons.put(SecureKeyboard.EMPTY_KEY_CODE,getResources().getDrawable(R.drawable.icon_1, null));
        sc.changeKeyIcons(icons);

        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                SkDecoder skDecoder = new SkDecoder();
                String usrName = skDecoder.decode(sc.getText(username));
                String pwd = skDecoder.decode(sc.getText(password));
                Log.d(TAG, "onClick usrName: "+usrName);
                Log.d(TAG, "onClick pwd: "+pwd);

                if(usrName.equals("admin") && pwd.equals("pwd")){
                    LogoutActivity();
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Login with wrong username password...!",Toast.LENGTH_SHORT).show();
                    password.setBackgroundColor(Color.RED);
                    loginButton.setVisibility(View.VISIBLE);
                }
            }

        });

        final Button pinLogoutButton = findViewById(R.id.pinLogin);
        pinLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PinLogoutActivity();
                finish();
            }
        });
    }

    public void PinLogoutActivity(){
        Intent intent = new Intent(this, PinLogInActivity.class);
        startActivity(intent);
    }

    public void LogoutActivity(){
        Intent intent = new Intent(this, LogoutActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        sc.onBackPressed(this);
    }
}

