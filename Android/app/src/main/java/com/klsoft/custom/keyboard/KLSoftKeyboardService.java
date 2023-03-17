package com.klsoft.custom.keyboard;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.klsoft.custom.R;
import com.klsoft.custom.checksum.CRC8;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class KLSoftKeyboardService implements KeyboardView.OnKeyboardActionListener {
    private final KeyboardView keyboardView;
    private KLSoftKeyboard.KeyboardType keyboardType;
    private boolean isSymbol = false;
    static final int TEXT_KEYBOARD = 0;
    static final int NUMBER_KEYBOARD = 1;
    private int prevOrientation = Configuration.ORIENTATION_UNDEFINED;
    private EditText editText;
    HashMap<Integer, Float> editTextYList = new HashMap<>();
    HashMap<Integer, Drawable> editTextBGList = new HashMap<>();
    //private static final SimpleDateFormat currentFormatDate = new SimpleDateFormat("yyyyMMdd");
    boolean isMoveEditText = false;
    private final AppCompatActivity activity;
    private KLSoftKeyboard klSoftKeyboard;
    private CRC8 crc8;
    private final Charset charset = Charset.forName("UTF-8");
    private float keyHeight;
    //create hashmap for store real value of the TextBox
    HashMap<EditText,String> input_values;
    HashMap<Character, Drawable> key_icons;
    HashMap<EditText, Boolean> isEmptyKeyLayout;
    HashMap<EditText, Boolean> isEnableEmptyKeyIcon;
    String replacement = "*";
    private boolean isShiftLongPressed = false;
    Timer timer;
    String TAG="KLSoft";
    public KLSoftKeyboardService(AppCompatActivity activity) {
        this.activity = activity;
        input_values = new HashMap<>();
        isEmptyKeyLayout = new HashMap<>();
        isEnableEmptyKeyIcon = new HashMap<>();
        //timerLongPress = new Timer();
        keyboardView = activity.findViewById(R.id.keyboard);
        keyboardView.setOnKeyboardActionListener(this);
        this.keyHeight = dpToPixel(50); //50dp is set in res->xml->keyboard.xm.
        // Hide the standard keyboard initially
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    float dpToPixel(float dp){
        float dip = dp;
        Resources r = activity.getResources();
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                r.getDisplayMetrics()
        );
    }


    class RemindTask extends TimerTask {
        public void run() {
            //Log.d(TAG, "Time's up!");
            isShiftLongPressed = true;
            timer.cancel(); //Terminate the timer thread
        }
    }
    @Override
    public void onPress(final int primaryCode) {
        //prevent popup key to move from symbol or refresh keys' position after those keys are clicked.
        keyboardView.setPopupOffset(0,0);
        //show text in editText, show real value instead of dot
        if (primaryCode == KLSoftKeyboard.CONFIRM_KEY_CODE && editText.getText().toString().contains(replacement)){
            //editText.setTransformationMethod(null);
            editText.setText(input_values.get(editText));
            moveEditTextCursorToLastIndex();
        }
        if(primaryCode == KLSoftKeyboard.SHIFT_KEY_CODE){
            isShiftLongPressed = false;
            timer = new Timer();
            timer.schedule(new RemindTask(), 1000);//one second
        }
    }

    @Override
    public void onRelease(int primaryCode) {
        if(primaryCode == KLSoftKeyboard.SHIFT_KEY_CODE) {
            timer.cancel();
        }
        //Hide text in editText, show dot instead of real value
        if (primaryCode == KLSoftKeyboard.CONFIRM_KEY_CODE ){
            //editText.setTransformationMethod(new PasswordTransformationMethod());
            replaceTextInEditTextWith(replacement);
        }
    }

    @Override
    public void onText(CharSequence text) {}

    @Override
    public void swipeLeft() {}

    @Override
    public void swipeRight() {}

    @Override
    public void swipeDown() {}

    @Override
    public void swipeUp() {}

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        //Log.d(TAG, "On key..... " + isLongPressed);
        //use try catch to prevent random crush, index  out of bound error
        try {
            InputConnection ic = new CustomInputConnection(editText);
            String input_v = input_values.get(editText) == null ? "" : input_values.get(editText);
            if(!isShiftLongPressed && (primaryCode != KLSoftKeyboard.SHIFT_KEY_CODE)) {
                keyboardView.setShifted(false);
            }
            switch (primaryCode) {
                case KLSoftKeyboard.DELETE_KEY_CODE:
                    ic.deleteSurroundingText(1, 0);
                    if (!input_v.equals("")) {
                        input_v = input_v.substring(0, input_v.length() - 1);
                    }
                    break;

                case KLSoftKeyboard.SHIFT_KEY_CODE:
                    Drawable shiftIcon;
                    if(isShiftLongPressed){
                        shiftIcon = activity.getResources().getDrawable(R.drawable.icon_key_shift_cap, null);
                    }
                    else{
                        shiftIcon = activity.getResources().getDrawable(R.drawable.icon_shift_key, null);
                    }
                    changeKeyIcon(klSoftKeyboard.getKeys().get(KLSoftKeyboard.SHIFT_KEY_INDEX), shiftIcon);

                    keyboardView.setShifted(!keyboardView.isShifted());
                    break;

                case KLSoftKeyboard.ENTER_KEY_CODE:
                    hideCustomKeyboard();
                    break;

                case KLSoftKeyboard.SPACE_KEY_CODE:
                    ic.commitText(" ", 1);
                    break;

                case KLSoftKeyboard.REFRESH_KEY_CODE:
                    if (keyboardType == KLSoftKeyboard.KeyboardType.NUMBER) {
                        setKeyboard(activity, NUMBER_KEYBOARD);
                    } else {
                        setKeyboard(activity, TEXT_KEYBOARD);
                    }
                    break;

                case KLSoftKeyboard.SYMBOL_KEY_CODE:
                    if (isSymbol) {
                        isSymbol = false;
                    } else {
                        isSymbol = true;
                    }
                    setKeyboard(activity, TEXT_KEYBOARD);
                    break;

                /*case KLSoftKeyboard.CONFIRM_KEY_CODE:*/
                case KLSoftKeyboard.EMPTY_KEY_CODE:
                    break;

                default:
                    char code = (char) primaryCode;
                    String c;
                    if (Character.isLetter(code)) {
                        if (keyboardView.isShifted()) code = Character.toUpperCase(code);
                        c = String.valueOf(code);
                    } else {
                        c = String.valueOf(code);
                    }
                    replaceTextInEditTextWith(replacement);
                    ic.commitText(c, 1);
                    input_v += c;
            }
            //add or update text in hashmap
            input_values.put(editText, input_v);
        }catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
    }

    private void replaceTextInEditTextWith(String replaceStr){
        if(!editText.getText().toString().equals("")){
            String tmp = editText.getText().toString();
            editText.setText(tmp.replaceAll(".", replaceStr));
            moveEditTextCursorToLastIndex();
        }
    }
    private void setKeyboard(AppCompatActivity activity, int inputType) {
        if(inputType == NUMBER_KEYBOARD){
            if(isEmptyKeyLayout.get(editText)){
                KLSoftKeyboard.NUM_KEY_ROWS = 5;
            }else{
                KLSoftKeyboard.NUM_KEY_ROWS = 4;
            }
            KLSoftKeyboard.NUM_KEY_COLS = 3;
            keyboardType = KLSoftKeyboard.KeyboardType.NUMBER;
        }else{
            KLSoftKeyboard.NUM_KEY_ROWS = 5;
            KLSoftKeyboard.NUM_KEY_COLS = 11;
            if (isSymbol) {
                keyboardType = KLSoftKeyboard.KeyboardType.SYMBOL;
            } else {
                keyboardType = KLSoftKeyboard.KeyboardType.ENGLISH;
            }
        }
        KLSoftKeyboard.isEnableEmptyKeyIcon = isEnableEmptyKeyIcon.get(editText);
        KLSoftKeyboard.isEmptyKeyLayout = isEmptyKeyLayout.get(editText);
        klSoftKeyboard = new KLSoftKeyboard(activity, R.xml.keyboard, keyboardType, keyHeight);
        keyboardView.setKeyboard(klSoftKeyboard);

        if(inputType == TEXT_KEYBOARD){ //English keyboard
            if(keyboardType == KLSoftKeyboard.KeyboardType.SYMBOL){
                klSoftKeyboard.getKeys().get(KLSoftKeyboard.getSymbolKeyIndex()).label = "1Aa";
            }else{
                klSoftKeyboard.getKeys().get(KLSoftKeyboard.getSymbolKeyIndex()).label = "!@#";
            }
        }

        if(key_icons != null){
            List<Keyboard.Key> keys = klSoftKeyboard.getKeys();
            for (Keyboard.Key k : keys
            ) {
                Drawable icon = null;
                if(k.label != null) {
                    icon = key_icons.get(k.label.charAt(0));
                }else{
                    switch (k.codes[0]){
                        case KLSoftKeyboard.EMPTY_KEY_CODE:
                            if(isEnableEmptyKeyIcon.get(editText)) {
                                icon = key_icons.get(KLSoftKeyboard.EMPTY_KEY_CODE);
                            }
                            break;
                        case KLSoftKeyboard.CONFIRM_KEY_CODE:
                            icon = key_icons.get(KLSoftKeyboard.CONFIRM_KEY_CODE);
                            break;
                        case KLSoftKeyboard.DELETE_KEY_CODE:
                            icon = key_icons.get(KLSoftKeyboard.DELETE_KEY_CODE);
                            break;
                        case KLSoftKeyboard.SHIFT_KEY_CODE:
                            icon = key_icons.get(KLSoftKeyboard.SHIFT_KEY_CODE);
                            break;
                        case KLSoftKeyboard.ENTER_KEY_CODE:
                            icon = key_icons.get(KLSoftKeyboard.ENTER_KEY_CODE);
                            break;
                        case KLSoftKeyboard.REFRESH_KEY_CODE:
                            icon = key_icons.get(KLSoftKeyboard.REFRESH_KEY_CODE);
                            break;
                        case KLSoftKeyboard.SPACE_KEY_CODE:
                            icon = key_icons.get(KLSoftKeyboard.SPACE_KEY_CODE);
                            break;
                        case KLSoftKeyboard.SYMBOL_KEY_CODE:
                            icon = key_icons.get(KLSoftKeyboard.SYMBOL_KEY_CODE);
                            break;
                    }
                }
                if (icon != null) {
                    k.icon = icon;
                    k.label = null;
                }
            }
        }
    }


    protected boolean isCustomKeyboardVisible() {
        return keyboardView.getVisibility() == View.VISIBLE;
    }

    private void showCustomKeyboard(View v, int inputType) {
        keyboardView.setVisibility(View.VISIBLE);
        keyboardView.setEnabled(true);
        this.editText = (EditText) v;
        if (v != null)
            ((InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(v.getWindowToken(), 0);
        showWindow(inputType);
    }

    public void hideCustomKeyboard() {
        if(editTextBGList.get(editText.getId()) != null) {
            editText.setY(editTextYList.get(editText.getId()));
            editText.setBackground(editTextBGList.get(editText.getId()));
        }
        keyboardView.setVisibility(View.GONE);
        keyboardView.setEnabled(false);
    }
    private void showWindow(int inputType) {
        int orientation = activity.getResources().getConfiguration().orientation;
        if (prevOrientation != orientation) {
            prevOrientation = orientation;
            editTextBGList.put(editText.getId(), null);
        }
        setKeyboard(activity, inputType);

        keyboardView.post(() -> {
            int editTextLoc = keyboardView.getTop() - klSoftKeyboard.getDefaultKeyHeight() - 25;
            isMoveEditText = editText.getY() > editTextLoc;
            if(isMoveEditText) {
                if(editTextBGList.get(editText.getId()) == null) {
                    editTextYList.put(editText.getId(), editText.getY());
                    editTextBGList.put(editText.getId(),editText.getBackground());
                }
                editText.setBackgroundColor(Color.parseColor("#DDF9F5"));
                editText.setY(editTextLoc);
            }
        });

        moveEditTextCursorToLastIndex();
        //if (isFirstPopUp) {
        //validate keyboard first popup arrange incorrectly
        AtomicInteger i = new AtomicInteger();
        final View activityRootView = keyboardView.findViewById(R.id.keyboard);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
            if (heightDiff > 0 && i.get() < 1) {
                setKeyboard(activity, inputType);
                i.getAndIncrement();
            }
        });
        //}
    }
    private  void moveEditTextCursorToLastIndex(){
        if (editText != null)
            editText.setSelection(editText.getText().length());
    }
    /**
     * isEmptyKeyLayout = true -> Insert empty key to keyboard layout, otherwise false
     * call before registerEditText is called.
     * */
    public void setEmptyKeyLayout(EditText editText, boolean isEmptyKeyLayout){
        this.isEmptyKeyLayout.put(editText, isEmptyKeyLayout);
    }
    public void setEnableEmptyKeyIcon(EditText editText, boolean isEnableEmptyKeyIcon){
        this.isEnableEmptyKeyIcon.put(editText, isEnableEmptyKeyIcon);
    }
    //inputType: 0: number keyboard
    //           1: text keyboard
    public void registerEditText(EditText editText, int inputType) {
        try {
            this.editText = editText;
            //this.editText.setTransformationMethod(new PasswordTransformationMethod());
            this.editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus)
                        showCustomKeyboard(view, inputType);
                    else
                        hideCustomKeyboard();
                }
            });

            this.editText.setOnClickListener(v1 -> showCustomKeyboard(v1, inputType));
            this.editText.setOnTouchListener((v, event) -> {
                EditText edittext1 = (EditText) v;
                int inType = edittext1.getInputType();          // Backup the input type
                edittext1.setInputType(InputType.TYPE_NULL);    // Disable standard keyboard
                edittext1.onTouchEvent(event);                  // Call native handler
                edittext1.setInputType(inType);                 // Restore input type
                return true;                                    // Consume touch event
            });
            //this.editText.setInputType(editText.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            this.editText.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        }catch (Exception e){
            log(e.getMessage());
        }
    }

    public String getText(EditText editText){
        if(input_values.get(editText) != null) {
            try {
                //get current time
                Date date = new Date();
                long curTime = date.getTime();
                //convert long to string
                String strTime = String.valueOf(curTime);
                //get 8 characters from strTime
                String time = strTime.substring(strTime.length() - 8);
                //convert string to byte arr
                byte[] byteStrTime = time.getBytes(charset);
                //get string's crc8
                byte k = crc8.calc(byteStrTime, byteStrTime.length);
                //get original text input
                String input_v = input_values.get(editText);
                //convert to byte
                byte[] bInput_v = input_v.getBytes(charset);
                byte[] enc_v = new byte[bInput_v.length];
                int i = 0;
                for (byte c : bInput_v) {
                    enc_v[i] = (byte) (c ^ k);
                    i++;
                }
                java.util.zip.CRC32 crc32 = new java.util.zip.CRC32();
                crc32.update(bInput_v);
                //String checkSum = Long.toHexString(crc32.getValue());
                //convert crc32's value to hex string
                String checkSum = String.format("%08X", crc32.getValue());
                return time + bytesToHex(enc_v) + checkSum;
            } finally {
                //clear the values
                editText.setText("");
                input_values.remove(editText);
            }
        }else return "";
    }
    final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(Charset.forName("ASCII"));
    String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, charset);
    }

    public void changeKeyIcons(HashMap<Character, Drawable> keyIcons){
        //assign icons' hashmap to key_icons so that we can replace key icon after key initialize complete
        this.key_icons = keyIcons;
    }
    void changeKeyIcon(Keyboard.Key k, Drawable keyIcon){
        if(keyIcon != null) {
            k.icon = keyIcon;
            k.label = null;
        }
    }
    public void log(String message) {
        Log.d("KLSoft", "message:  " + message);
    }

    public void toast(String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }
}
