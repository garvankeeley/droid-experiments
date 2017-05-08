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
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.security.auth.x500.X500Principal;

public class Keystore {

    static final String PREFS_NAME = "logins-prefs";
    SharedPrefService prefs;


    public enum InitResult {
        EMPTY_STORE, NONEMPTY_STORE
    }

    public InitResult init(Context context) {
        prefs = new SharedPrefService(context, PREFS_NAME);


        Qlassified.Service.start(context);
        Qlassified.Service.setStorageService(prefs);
//        Qlassified.Service.put("SomeKey", "SomeValue");
        String pin = Qlassified.Service.getString("PIN");
        return (pin != null) ? InitResult.NONEMPTY_STORE : InitResult.EMPTY_STORE;
    }

    class SharedPrefService extends QlassifiedStorageService {

        final SharedPreferences preferences;

        public SharedPrefService(Context context, String storageName) {
            this.preferences = context.getSharedPreferences(storageName, Context.MODE_PRIVATE);
        }

        @Override
        public void onSaveRequest(EncryptedEntry encryptedEntry) {
            SharedPreferences.Editor editor = this.preferences.edit();
            editor.putString(encryptedEntry.getKey(), encryptedEntry.getEncryptedValue());
            editor.apply(); // apply is async
            Log.d("Storage", String.format("Saved key: %s", encryptedEntry.getKey()));
            Log.d("Storage", String.format("Saved encrypted value: %s", encryptedEntry.getEncryptedValue()));
        }

        @Override
        public EncryptedEntry onGetRequest(String key) {
            Log.d("Storage", String.format("Get by key: %s", key));
            String encryptedValue = this.preferences.getString(key, null);
            Log.d("Storage", String.format("Got encrypted value: %s", encryptedValue));
            return new EncryptedEntry(key, encryptedValue);
        }

//        void putMultiple(Map<String, String> dict) {
//           for () {
//               EncryptedEntry encryptedEntry = keyStore.encryptEntry(new QlassifiedString(key, value));
//           }
//        }

    }

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
