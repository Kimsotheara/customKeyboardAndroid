package com.klsoft.custom.keyboard;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.EditText;

import com.klsoft.custom.R;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class KLSoftKeyboard extends Keyboard {

    static final char SHIFT_KEY_CODE = '\1';
    static final char SPACE_KEY_CODE = '\2';
    static final char SYMBOL_KEY_CODE = '\3';
    static final char ENTER_KEY_CODE = '\4';
    static final char DELETE_KEY_CODE = '\5';
    static final char REFRESH_KEY_CODE = '\6';
    static final char EMPTY_KEY_CODE = '\7';
    static final char CONFIRM_KEY_CODE = '\u039E';

    static int SHIFT_KEY_INDEX;
    static int DELETE_KEY_INDEX;
    static int SYMBOL_KEY_INDEX;
    static int REFRESH_KEY_INDEX;
    static int SPACE_KEY_INDEX;
    static int ENTER_KEY_INDEX;
   /* private static int CONFIRM_KEY_INDEX;*/
    static int NUM_KEY_ROWS ;
    static int NUM_KEY_COLS;
    static boolean isEmptyKeyLayout;
    static boolean isEnableEmptyKeyIcon;
    private final Context context;
    private int keyHeight;
    private final float keyboardHeight;
    enum KeyboardType {
        ENGLISH,
        SYMBOL,
        NUMBER
    }
    private static final char[] numberKey_0 =
            {
                    '1','2', '3',
                    '4','5', EMPTY_KEY_CODE,
                    '6','7','8',
                    '9', '0',EMPTY_KEY_CODE,
                    REFRESH_KEY_CODE,DELETE_KEY_CODE,ENTER_KEY_CODE

            };
    private static final char[] numberKey_1 =
            {
                    '1','2', '3',
                    '4','5', '6',
                    '7','8', '9',
                    DELETE_KEY_CODE,'0',ENTER_KEY_CODE

            };
    private static final char[] englishKeys =
            {
                    '1', '2', '3', '4', '5', '6', '7', '8', '9', '0',EMPTY_KEY_CODE,
                    'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p',EMPTY_KEY_CODE,
                    'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l',EMPTY_KEY_CODE,EMPTY_KEY_CODE,
                    SHIFT_KEY_CODE, 'z', 'x', 'c', 'v', 'b', 'n', 'm',EMPTY_KEY_CODE, DELETE_KEY_CODE,
                    REFRESH_KEY_CODE,SYMBOL_KEY_CODE, SPACE_KEY_CODE, ENTER_KEY_CODE
            };

    private static final char[] symbolKeys =
            {
                    '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', EMPTY_KEY_CODE,
                    '~', '`', '_', '-', '=', '+', '\'', '|', EMPTY_KEY_CODE, EMPTY_KEY_CODE,EMPTY_KEY_CODE,
                    ';', ';', '\\', '"', '[', ']', '{', '}', EMPTY_KEY_CODE, EMPTY_KEY_CODE,EMPTY_KEY_CODE,
                    '<', '>', ',', '.', '?', '/', EMPTY_KEY_CODE, EMPTY_KEY_CODE,EMPTY_KEY_CODE,DELETE_KEY_CODE,
                    REFRESH_KEY_CODE,SYMBOL_KEY_CODE, SPACE_KEY_CODE, ENTER_KEY_CODE
            };

    KLSoftKeyboard(Context context, int xmlLayoutResId, KeyboardType keyboardType, float keyboardHeight) {
        super(context, xmlLayoutResId, getKeyCharacters(keyboardType), -1, 0);
        //Log.d("KLSoft", "isEnableEmptyKeyIcon(constr): "+ isEnableEmptyKeyIcon);
        this.context = context;
        this.keyboardHeight = keyboardHeight;
        if(keyboardType == keyboardType.NUMBER){
            changeKeyHeightOfNumberPad();
        }else{
            if (keyboardType == keyboardType.SYMBOL){
                addBlankKeySymbols();
                changeKeyHeightSymbols();
            }else {
                addBlankKeys();
                changeKeyHeight();
            }
        }
    }

    private static CharSequence getKeyCharacters(KeyboardType keyboardType){
        char[] ranKeyboard;
        if(keyboardType == keyboardType.NUMBER){
            if(isEmptyKeyLayout){
                ranKeyboard = numberKey_0;
            }else{
                ranKeyboard = numberKey_1;
            }
            randomNumberKeyboard(ranKeyboard);
        } else if (keyboardType == KeyboardType.SYMBOL) {
            ranKeyboard = symbolKeys;
        } else {
            ranKeyboard = englishKeys;
        }
        setSpecialKeyIndex(ranKeyboard);
        return new String(ranKeyboard);
    }

    private static void randomNumberKeyboard(char[] ranKeyboard){
        char[] numberArr = {'0','1','2','3','4','5','6','7','8','9',EMPTY_KEY_CODE,EMPTY_KEY_CODE};
        shuffleArray(numberArr);
        int keyIndex = 0;
        for(int i=0; i<numberArr.length; i++,keyIndex++){
            if(!isEmptyKeyLayout) {
                if (numberArr[i] == EMPTY_KEY_CODE) i++;
                if (ranKeyboard[keyIndex] == DELETE_KEY_CODE ||
                        ranKeyboard[keyIndex] == ENTER_KEY_CODE) keyIndex++;
            }
            if(keyIndex < ranKeyboard.length) {
                ranKeyboard[keyIndex] = numberArr[i];
            }
        }
    }
    static void shuffleArray(char[] ar)
    {
        Random rnd = ThreadLocalRandom.current();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            char a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }
    private void addBlankKeySymbols(){
        //First row
        repositionKeys(0, 10, NUM_KEY_COLS);// 10 is original icon key index
        //Second row
        repositionKeys(11,19, NUM_KEY_COLS);// 19 is original icon key index
        repositionKeys(11,20, NUM_KEY_COLS);// 20 is original icon key index
        repositionKeys(11,21, NUM_KEY_COLS);// 21 is original icon key index
        //third row, have two icon keys
        repositionKeys(22,30, NUM_KEY_COLS);// 30 is original icon key index
        repositionKeys(22,31, NUM_KEY_COLS);// 31 is original icon key index
        repositionKeys(22,32, NUM_KEY_COLS);// 32 is original icon key index
        //last row
        repositionKeys(33,39, NUM_KEY_COLS-2);//-2 for shift key and delete key
        repositionKeys(33,40, NUM_KEY_COLS-2);//-2 for shift key and delete key
        repositionKeys(33,41, NUM_KEY_COLS-2);//-2 for shift key and delete key

    }

    private void addBlankKeys(){
        repositionKeys(0, 10, NUM_KEY_COLS);// 10 is original icon key index
        repositionKeys(11,21, NUM_KEY_COLS);// 21 is original icon key index
        //third row, have two icon keys
        repositionKeys(22,31, NUM_KEY_COLS);// 31 is original icon key index
        repositionKeys(22,32, NUM_KEY_COLS);// 32 is original icon key index
        //last row
        repositionKeys(34,41, NUM_KEY_COLS-3);//-2 for shift key and delete key

    }
    private void repositionKeys(int min, int iconKeyPosition, int numKeyInRow){
        List<Key> keys = getKeys();
        //int max = min + numKeyInRow;
        int ranPost = new Random().nextInt(numKeyInRow) + min;
        CharSequence tmpKeyLabel = keys.get(ranPost).label;
        int[] tmpKeyCodes =  keys.get(ranPost).codes;
        keys.get(ranPost).label = keys.get(iconKeyPosition).label;
        keys.get(ranPost).codes = keys.get(iconKeyPosition).codes;
        for (int i = ranPost + 1; i < min + numKeyInRow ; i++) {
            CharSequence nextTmpKeyLabel = keys.get(i).label;
            int[] nextTmpKeyCodes = keys.get(i).codes;
            keys.get(i).label = tmpKeyLabel;
            keys.get(i).codes = tmpKeyCodes;
            tmpKeyLabel =  nextTmpKeyLabel;
            tmpKeyCodes = nextTmpKeyCodes;
        }
    }

    private static void setSpecialKeyIndex(char[] chars) {
        for (int i = 0; i < chars.length; i++) {
            char ch = chars[i];
            switch (ch) {
                case SPACE_KEY_CODE:
                    SPACE_KEY_INDEX = i;
                    break;
                case SHIFT_KEY_CODE:
                    SHIFT_KEY_INDEX = i;
                    break;
                case SYMBOL_KEY_CODE:
                    SYMBOL_KEY_INDEX = i;
                    break;
                case ENTER_KEY_CODE:
                    ENTER_KEY_INDEX = i;
                    break;
                case DELETE_KEY_CODE:
                    DELETE_KEY_INDEX = i;
                    break;
                case REFRESH_KEY_CODE:
                    REFRESH_KEY_INDEX = i;
                    break;
               /* case CONFIRM_KEY_CODE:
                    CONFIRM_KEY_INDEX = i;
                    break;*/
                default:
                    break;
            }
        }
    }

    @Override
    public int[] getNearestKeys(int x, int y) {
        //Re-calculate keys' position
        List<Key> keys = getKeys();
        for (int i = 0; i < keys.size(); i++) {
            int startX = keys.get(i).x;
            int endX = startX + keys.get(i).width;
            int startY = keys.get(i).y;
            int endY = startY + keys.get(i).height;
            if (x > startX && x < endX && y > startY && y < endY) {
                return new int[]{i};
            }
        }
        return new int[0];
    }

    @Override
    public int getHeight() {
        return getKeyHeight() * NUM_KEY_ROWS;
    }

    public static int getSymbolKeyIndex() {
        return SYMBOL_KEY_INDEX;
    }

    private void changeKeyHeightSymbols() {
        List<Key> keys = getKeys();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        int defaultHeight = (int)keyboardHeight;
        int defaultWidth = (screenWidth / (NUM_KEY_COLS));
        //fill width/height gap in case last digit of screenWidth/screenHeight is bigger than 0. eg. 768
        if(screenWidth % NUM_KEY_COLS != 0)
            defaultWidth += 1;
        if(screenHeight % NUM_KEY_COLS != 0)
            defaultHeight += 1;
        int width = defaultWidth;
        int key_y = 0;
        int key_x = 0;
        int totalWidth = 0;

        for (int i = 0; i < keys.size(); i++) {
            Key key = keys.get(i);
            String[] special_key_index = new String[]{
//                    Integer.toString(SHIFT_KEY_INDEX),
                    Integer.toString(DELETE_KEY_INDEX),
                    Integer.toString(REFRESH_KEY_INDEX),
                    Integer.toString(SYMBOL_KEY_INDEX)
            };
            boolean isSpcKeyIndex = Arrays.asList(special_key_index)
                    .contains(Integer.toString(i));
            if (isSpcKeyIndex) {
                width = width * 2;
            } else if (i == ENTER_KEY_INDEX) {
                width = width * 3;
            } else if (i == SPACE_KEY_INDEX) {
                width = width * 4;
            }
            key.width = width;
            key.height = defaultHeight;
            totalWidth += width;

            key.x = key_x;
            key.y = key_y; //by default, key.y is set to next row every 10 columns.
            key_x += width;

            if (totalWidth >= screenWidth) {
                key_y += defaultHeight;
                key_x = 0;
                totalWidth = 0;
            }
            width = defaultWidth;
            keyHeight = defaultHeight;

            setEmptyKey(key);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Key key = keys.get(SHIFT_KEY_INDEX);
            key = keys.get(DELETE_KEY_INDEX);
            key.label = null;
            key.icon = context.getResources().getDrawable(R.drawable.icon_delete_key, null);
            key.repeatable = true;
            key = keys.get(SPACE_KEY_INDEX);
            key.label = "SPACE";
            key = keys.get(SYMBOL_KEY_INDEX);
            key.label = "!@#";
            key = keys.get(ENTER_KEY_INDEX);
            key.label = "입력완료";
            key.popupCharacters = "입력완료";
            key.icon = context.getResources().getDrawable(R.drawable.icon_enter_key, null);
            key = keys.get(REFRESH_KEY_INDEX);
            key.label = "재배열";
            key.popupCharacters = "재배열";
            key.icon = context.getResources().getDrawable(R.drawable.icon_refresh_key, null);
        }
    }

    private void changeKeyHeight() {
        List<Key> keys = getKeys();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        int defaultHeight = (int)keyboardHeight;
        int defaultWidth = (screenWidth / (NUM_KEY_COLS));
        //fill width/height gap in case last digit of screenWidth/screenHeight is bigger than 0. eg. 768
        if(screenWidth % NUM_KEY_COLS != 0)
            defaultWidth += 1;
        if(screenHeight % NUM_KEY_COLS != 0)
            defaultHeight += 1;
        int width = defaultWidth;
        int key_y = 0;
        int key_x = 0;
        int totalWidth = 0;

        for (int i = 0; i < keys.size(); i++) {
            Key key = keys.get(i);
            String[] special_key_index = new String[]{
//                    Integer.toString(SHIFT_KEY_INDEX),
                    Integer.toString(DELETE_KEY_INDEX),
                    Integer.toString(REFRESH_KEY_INDEX),
                    Integer.toString(SYMBOL_KEY_INDEX)
            };
            boolean isSpcKeyIndex = Arrays.asList(special_key_index)
                    .contains(Integer.toString(i));
            if (isSpcKeyIndex) {
                width = width * 2;
            } else if (i == ENTER_KEY_INDEX) {
                width = width * 3;
            } else if (i == SPACE_KEY_INDEX) {
                width = width * 4;
            }
            key.width = width;
            key.height = defaultHeight;
            totalWidth += width;

            key.x = key_x;
            key.y = key_y; //by default, key.y is set to next row every 10 columns.
            key_x += width;

            if (totalWidth >= screenWidth) {
                key_y += defaultHeight;
                key_x = 0;
                totalWidth = 0;
            }
            width = defaultWidth;
            keyHeight = defaultHeight;

            setEmptyKey(key);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
           /* Key key = keys.get(CONFIRM_KEY_INDEX);
            key.label = null;
            key.icon = context.getResources().getDrawable(R.drawable.icon_confirm_key, null);*/
            Key key = keys.get(SHIFT_KEY_INDEX);
            key.label = null;
            key.icon = context.getResources().getDrawable(R.drawable.icon_shift_key, null);
            key = keys.get(DELETE_KEY_INDEX);
            key.label = null;
            key.icon = context.getResources().getDrawable(R.drawable.icon_delete_key, null);
            key.repeatable = true;
            key = keys.get(SPACE_KEY_INDEX);
            key.label = "SPACE";
            key = keys.get(SYMBOL_KEY_INDEX);
            key.label = "!@#";
            key = keys.get(ENTER_KEY_INDEX);
            key.label = "입력완료";
            key.icon = context.getResources().getDrawable(R.drawable.icon_enter_key, null);
            key = keys.get(REFRESH_KEY_INDEX);
            key.label = "재배열";
            key.icon = context.getResources().getDrawable(R.drawable.icon_refresh_key, null);
        }
    }
    private void changeKeyHeightOfNumberPad() {
        List<Key> keys = getKeys();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        int defaultHeight = (int)keyboardHeight;
        int defaultWidth = (screenWidth / (NUM_KEY_COLS));
        //fill width/height gap in case last digit of screenWidth/screenHeight is bigger than 0. eg. 768
        if(screenWidth % NUM_KEY_ROWS != 0)
            defaultWidth += 1;
        if(screenHeight % NUM_KEY_COLS != 0)
            defaultHeight += 1;
        int width = defaultWidth;
        int key_y = 0;
        int key_x = 0;
        int totalWidth = 0;

        for (int i = 0; i < keys.size(); i++) {
            Key key = keys.get(i);
           if (i == ENTER_KEY_INDEX) {
                width = width * 1;
            }
            key.width = width;
            key.height = defaultHeight;
            totalWidth += width;
            //log("key_x: " + key_x + "  key-y: " + key_y);
            key.x = key_x;
            key.y = key_y; //by default, key.y is set to next row every 10 columns.
            key_x += width;

            if (totalWidth >= screenWidth) {
                key_y += defaultHeight;
                key_x = 0;
                totalWidth = 0;
            }
            width = defaultWidth;
            keyHeight = defaultHeight;
            //Log.d("KLSoft", "isEnableEmptyKeyIcon(keyheight) " + isEnableEmptyKeyIcon);
            setEmptyKey(key);

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            /*Key key = keys.get(CONFIRM_KEY_INDEX);
            key.label = null;
            key.icon = context.getResources().getDrawable(R.drawable.icon_confirm_key, null);*/
            Key key = keys.get(DELETE_KEY_INDEX);
            key.label = null;
            key.icon = context.getResources().getDrawable(R.drawable.icon_delete_key, null);
            key.repeatable = true;
            key = keys.get(ENTER_KEY_INDEX);
            key.label = "입력완료";
            key.icon = context.getResources().getDrawable(R.drawable.icon_enter_key, null);
            if(isEmptyKeyLayout) {
                key = keys.get(REFRESH_KEY_INDEX);
                key.label = "재배열";
                key.icon = context.getResources().getDrawable(R.drawable.icon_refresh_key, null);
            }
        }
    }

    private void setEmptyKey(Key key){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (key.label.toString().equals(Character.toString(EMPTY_KEY_CODE))) {
                    if(isEnableEmptyKeyIcon) {
                        key.icon = context.getResources().getDrawable(R.drawable.icon_empty_key, null);
                    }
                    key.label = null;
                    key.x = key.x + key.width / 2;
                    key.width = 0;
                }
            }
    }

    /*public void log(String message) {
        Log.d("KLSoft", "message:  " + message);
    }*/

    public int getDefaultKeyHeight(){
        return keyHeight;
    }

}
