package com.aturan.tnote;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class CryptUtil {
    private final String ALGORITHM = "Blowfish";
    private final String MODE = "Blowfish/CBC/PKCS5Padding";
    private final String IV = "abcdefgh";
    private final String PREF_NAME = "decrptkey";
    public final String ERROR = "ERROR";
    private String KEY;
    private final Context context;
    private final SharedPreferences preferences;
    private SharedPreferences.Editor editor;


    public CryptUtil(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public CryptUtil(Context context, String key) {
        this.context = context;
        this.KEY = key;
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public CryptUtil initKey() {
        return new CryptUtil(context, this.getKey());
    }

    public String encrypt(String value) {

        try {

            SecretKeySpec secretKeySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(MODE);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(IV.getBytes()));
            byte[] values = cipher.doFinal(value.getBytes());
            return Base64.encodeToString(values, Base64.DEFAULT);

        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | IllegalArgumentException e) {
            e.printStackTrace();
            return ERROR;
        }

    }

    public String decrypt(String value) {

        try {

            byte[] values = Base64.decode(value, Base64.DEFAULT);
            SecretKeySpec secretKeySpec = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(MODE);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(IV.getBytes()));
            return new String(cipher.doFinal(values));

        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | IllegalArgumentException e) {
            e.printStackTrace();
            return ERROR;
        }

    }

    public void setKey(String key) {
        editor = preferences.edit();
        editor.putString("key", key);
        editor.putBoolean("isFirstTimeView", true);
        editor.apply();
    }

    public String getKey() {
        return preferences.getString("key", "def");
    }

    public Boolean isFirstTimeView() {
        return preferences.getBoolean("isFirstTimeView", false);
    }

    public void setIsFirstTimeView(Boolean b) {
        editor = preferences.edit();
        editor.putBoolean("isFirstTimeView", b);
        editor.apply();
    }

}

