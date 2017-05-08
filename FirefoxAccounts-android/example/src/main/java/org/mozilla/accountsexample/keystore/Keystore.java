package org.mozilla.accountsexample.keystore;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.KeyPairGeneratorSpec;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.q42.qlassified.Entry.EncryptedEntry;
import com.q42.qlassified.Entry.QlassifiedString;
import com.q42.qlassified.Qlassified;
import com.q42.qlassified.Storage.QlassifiedSharedPreferencesService;
import com.q42.qlassified.Storage.QlassifiedStorageService;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.mozilla.accountsexample.AppGlobals;
import org.mozilla.gecko.sync.ExtendedJSONObject;
import org.mozilla.gecko.sync.NonObjectJSONException;
import org.mozilla.gecko.sync.repositories.domain.PasswordRecord;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

public class Keystore {

    public SecretKey makeSymmetricKey() {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(1);
        }
        keyGenerator.init(160); // TODO: check avail lengths
        return keyGenerator.generateKey();
    }

    public void foo() {
        SecretKey key = makeSymmetricKey();
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            cipher.init(Cipher.ENCRYPT_MODE, key);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }


        String clearText = "I am an Employee";
        try {
            byte[] clearTextBytes = clearText.getBytes("UTF8");
            byte[] cipherBytes = cipher.doFinal(clearTextBytes);
            String cipherText = new String(cipherBytes, "UTF8");

            try {
                cipher.init(Cipher.DECRYPT_MODE, key);
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
            byte[] decryptedBytes = cipher.doFinal(cipherBytes);
            String decryptedText = new String(decryptedBytes, "UTF8");

            System.out.println("Before encryption: " + clearText);
            System.out.println("After encryption: " + cipherText);
            System.out.println("After decryption: " + decryptedText);
        } catch (Exception ex) {
        }
    }

    class Encryptor {
        KeySpec key;

        Encryptor(byte[] key) {
            if (key.length != 32) throw new IllegalArgumentException();
            this.key = new SecretKeySpec(key, "AES");
        }

        // the output is sent to users
        byte[] encrypt(byte[] src) throws Exception {

            byte[] iv = cipher.getIV(); // See question #1
            assert iv.length == 12; // See question #2
            byte[] cipherText = cipher.doFinal(src);
            assert cipherText.length == src.length + 16; // See question #3
            byte[] message = new byte[12 + src.length + 16]; // See question #4
            System.arraycopy(iv, 0, message, 0, 12);
            System.arraycopy(cipherText, 0, message, 12, cipherText.length);
            return message;
        }

        // the input comes from users
        byte[] decrypt(byte[] message) throws Exception {
            if (message.length < 12 + 16) throw new IllegalArgumentException();
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec params = new GCMParameterSpec(128, message, 0, 12);
            cipher.init(Cipher.DECRYPT_MODE, key, params);
            return cipher.doFinal(message, 12, message.length - 12);
        }
    }

    static final String PREFS_NAME = "logins-prefs";
//    SharedPrefService prefs;


    public enum InitResult {
        EMPTY_STORE, NONEMPTY_STORE
    }

    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    // password mased
//    public static String encrypt(String password, String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
//        int iterations = 4096;
//        char[] chars = password.toCharArray();
//        byte[] salt = key.getBytes();
//        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 128 * 8);
//        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
//        byte[] hash = skf.generateSecret(spec).getEncoded();
//        return toHex(hash);
//    }


    public InitResult init(Context context) {


//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bm.compress(Bitmap.CompressFormat.PNG, 100, baos); // bm is the bitmap object
//        byte[] b = baos.toByteArray();



        byte[] keyStart = "this is a key".getBytes();
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("PBKDF2WithHmacSHA1");
        sr.setSeed(keyStart);
        kgen.init(128, sr); // 192 and 256 bits may not be available
        SecretKey skey = kgen.generateKey();
        byte[] key = skey.getEncoded();

        // encrypt
        byte[] encryptedData = encrypt(key,b);
        // decrypt
        byte[] decryptedData = decrypt(key,encryptedData);

//        prefs = new SharedPrefService(context, PREFS_NAME);

//
//        Qlassified.Service.start(context);
//        Qlassified.Service.setStorageService(prefs);
////        Qlassified.Service.put("SomeKey", "SomeValue");
//        String pin = Qlassified.Service.getString("PIN");
//        return (pin != null) ? InitResult.NONEMPTY_STORE : InitResult.EMPTY_STORE;
    }

//    class SharedPrefService extends QlassifiedStorageService {
//
//        final SharedPreferences preferences;
//
//        public SharedPrefService(Context context, String storageName) {
//            this.preferences = context.getSharedPreferences(storageName, Context.MODE_PRIVATE);
//        }
//
//        @Override
//        public void onSaveRequest(EncryptedEntry encryptedEntry) {
//            SharedPreferences.Editor editor = this.preferences.edit();
//            editor.putString(encryptedEntry.getKey(), encryptedEntry.getEncryptedValue());
//            editor.apply(); // apply is async
//            Log.d("Storage", String.format("Saved key: %s", encryptedEntry.getKey()));
//            Log.d("Storage", String.format("Saved encrypted value: %s", encryptedEntry.getEncryptedValue()));
//        }
//
//        @Override
//        public EncryptedEntry onGetRequest(String key) {
//            Log.d("Storage", String.format("Get by key: %s", key));
//            String encryptedValue = this.preferences.getString(key, null);
//            Log.d("Storage", String.format("Got encrypted value: %s", encryptedValue));
//            return new EncryptedEntry(key, encryptedValue);
//        }
//
////        void putMultiple(Map<String, String> dict) {
////           for () {
////               EncryptedEntry encryptedEntry = keyStore.encryptEntry(new QlassifiedString(key, value));
////           }
////        }
//
//    }

    public void savePasswordRecords(List<PasswordRecord> records) {
        for (PasswordRecord r : records) {
            saveRecord(r);
        }
    }

    void saveRecord(PasswordRecord record) {
        JSONObject obj = new JSONObject();
        obj.putAll(AppGlobals.passwordRecordToMap(record));
        Qlassified.Service.put("_record_" + record.id, obj.toJSONString());
    }

    public List<Map<String,String>> readAllRecords() {
        List<Map<String,String>> records = new ArrayList<>();

        Map<String, ?> all = prefs.preferences.getAll();
        for (final String k : all.keySet()) {
            if (!k.startsWith("_record_")) {
                continue;
            }
            String jsonString = Qlassified.Service.getString(k);
            String id = k.replace("_record_", "");

            JSONParser parser = new JSONParser();
            JSONObject json = null;
            try {
                json = (JSONObject) parser.parse(jsonString);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Map<String, String> record = new HashMap<>();
            for (Object key : json.keySet()) {
                record.put(key.toString(), json.get(key).toString());
            }
            records.add(record);

        }
        return records;
    }
}
