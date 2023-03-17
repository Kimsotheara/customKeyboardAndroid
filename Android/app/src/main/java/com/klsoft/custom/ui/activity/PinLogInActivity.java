package com.klsoft.custom.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.klsoft.custom.R;
import com.klsoft.custom.checksum.SkDecoder;
import com.klsoft.custom.keyboard.SecureKeyboard;

import java.util.HashMap;

public class PinLogInActivity extends AppCompatActivity {
    SecureKeyboard sc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_login);
        sc = findViewById(R.id.secure_keyboard);
        EditText pinNumber = findViewById(R.id.txtPinNumber);
        EditText pinNumber1 = findViewById(R.id.txtPinNumber1);
        EditText pinNumber2 = findViewById(R.id.txtPinNumber2);
        sc.registerNumberKeyboard(pinNumber, SecureKeyboard.LAYOUT_NO_EMPTY_KEY);
        sc.registerNumberKeyboard(pinNumber1, SecureKeyboard.LAYOUT_EMPTY_KEY_NO_ICON);
        sc.registerNumberKeyboard(pinNumber2, SecureKeyboard.LAYOUT_EMPTY_KEY_WITH_ICON);

        HashMap<Character, Drawable> icons = new HashMap<>();
        icons.put(SecureKeyboard.EMPTY_KEY_CODE,getResources().getDrawable(R.drawable.icon_1, null));
        icons.put('1', getResources().getDrawable(R.drawable.icon_2, null));
        icons.put('2', getResources().getDrawable(R.drawable.icon_3, null));
        sc.changeKeyIcons(icons);

        final Button loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                SkDecoder skDecoder = new SkDecoder();
                String getPin = skDecoder.decode(sc.getText(pinNumber));
                if(getPin != null && getPin.equals("1234")){
                    Intent intent_passcode = new Intent(PinLogInActivity.this, LogoutActivity.class);
                    startActivity(intent_passcode);
                }
                else{
                    Toast.makeText(getApplicationContext(), "Login with wrong Pin...!",Toast.LENGTH_SHORT).show();
                    pinNumber.setBackgroundColor(Color.RED);
                    loginButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    @Override
    public void onBackPressed() {
        sc.onBackPressed(this);
    }

}
