package com.example.garv.tableviewandroid;

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
import com.q42.qlassified.Qlassified;
import com.q42.qlassified.Storage.QlassifiedSharedPreferencesService;
import com.q42.qlassified.Storage.QlassifiedStorageService;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Map;

import javax.security.auth.x500.X500Principal;

public class MainActivity extends AppCompatActivity {

    static final String LOGINS_FILE = "logins-prefs";
    ListView table;
    ListAdapter listAdapter;
    ArrayList<String> keyAliases;
    KeyStore keyStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        table = (ListView) findViewById(R.id.tableview);

        SharedPrefService prefs = new SharedPrefService(this, "logins-store");

        Qlassified.Service.start(this);
        Qlassified.Service.setStorageService(prefs/**/);

        Qlassified.Service.put("SomeKey", "SomeValue");

        Qlassified.Service.getString("SomeKey");

        Map<String, ?> all = prefs.preferences.getAll();
        for (String k : all.keySet()) {
            String v = Qlassified.Service.getString(k);

            Log.d("", String.format("%s %s", k , v));
        }
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
            editor.apply();
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
    }
}
