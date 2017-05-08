package org.mozilla.accountsexample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.mozilla.accountsexample.keystore.Keystore;
import org.mozilla.gecko.sync.repositories.domain.PasswordRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    Keystore keystore = new Keystore();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Keystore.InitResult result = keystore.init(this);
        if (result == Keystore.InitResult.EMPTY_STORE) {
            // show login dialog and pin creation
            FxASync.show(this);
            FxASync.listener = new FxASync.FxASyncListener() {
                @Override
                public void onReceivedPasswordRecords(List<PasswordRecord> receivedRecords) {
                    keystore.savePasswordRecords(receivedRecords);

                    List<Map<String, String>> records = new ArrayList<>();
                    for (PasswordRecord r : receivedRecords) {
                        records.add(AppGlobals.passwordRecordToMap(r));
                    }
                    displayRecords(records);
                }
            };
        } else {
            displayRecords(keystore.readAllRecords());
        }
    }

    private void displayRecords(List<Map<String, String>> records) {
        for (Map<String, String> record : records) {
            Log.d("", record.toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FxASync.handleActivityResult(requestCode, resultCode);
    }
}