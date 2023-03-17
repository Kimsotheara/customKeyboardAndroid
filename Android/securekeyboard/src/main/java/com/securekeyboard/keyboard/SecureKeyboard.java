package com.securekeyboard.keyboard;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.securekeyboard.R;

import java.util.HashMap;

public class SecureKeyboard extends View {
    private KLSoftKeyboardService klSoftKeyboardService;
    public static final int LAYOUT_NO_EMPTY_KEY = 0;
    public static final int LAYOUT_EMPTY_KEY_NO_ICON = 1;
    public static final int LAYOUT_EMPTY_KEY_WITH_ICON = 2;
    public static final char EMPTY_KEY_CODE = '\7';;
    public void initializeViews(AppCompatActivity activity) {
        activity.setContentView(R.layout.layout_keyboard);
        klSoftKeyboardService = new KLSoftKeyboardService(activity);
        // Hide the standard keyboard initially
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
    }
    public SecureKeyboard(Context context, AttributeSet attSet){
        super(context, attSet);
        initializeViews((AppCompatActivity) context);
    }
    public void registerTextKeyboard(EditText editText, boolean isEnableEmptyKeyIcon){
        klSoftKeyboardService.setEmptyKeyLayout(editText,true);
        klSoftKeyboardService.setEnableEmptyKeyIcon(editText, isEnableEmptyKeyIcon);
        klSoftKeyboardService.registerEditText(editText, KLSoftKeyboardService.TEXT_KEYBOARD);
    }

    public void registerNumberKeyboard(EditText editText, int keyLayout){
        switch (keyLayout){
            case LAYOUT_NO_EMPTY_KEY:
                klSoftKeyboardService.setEmptyKeyLayout(editText,false);
                klSoftKeyboardService.setEnableEmptyKeyIcon(editText, false);
                break;
            case LAYOUT_EMPTY_KEY_NO_ICON:
                klSoftKeyboardService.setEmptyKeyLayout(editText,true);
                klSoftKeyboardService.setEnableEmptyKeyIcon(editText,false);
                break;
            default:
                klSoftKeyboardService.setEmptyKeyLayout(editText,true);
                klSoftKeyboardService.setEnableEmptyKeyIcon(editText,true);
        }
        klSoftKeyboardService.registerEditText(editText, KLSoftKeyboardService.NUMBER_KEYBOARD);
    }

    public String getText(EditText editText){
            return klSoftKeyboardService.getText(editText);
    }
    /**
     *
     * @param icons: HashMap's key: key's label
     *               HashMap's value: icon to replace
     */
    public void changeKeyIcons(HashMap<Character, Drawable> icons){
        klSoftKeyboardService.changeKeyIcons(icons);
    }
    public void onBackPressed(AppCompatActivity activity) {
        if (klSoftKeyboardService.isCustomKeyboardVisible())
            klSoftKeyboardService.hideCustomKeyboard();
        else activity.finish();
    }
}
