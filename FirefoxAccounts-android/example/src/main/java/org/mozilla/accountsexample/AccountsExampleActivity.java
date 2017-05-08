package org.mozilla.accountsexample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import org.mozilla.accounts.FirefoxAccount;
import org.mozilla.accounts.FirefoxAccountDevelopmentStore;
import org.mozilla.accounts.FirefoxAccountEndpointConfig;
import org.mozilla.accounts.login.FirefoxAccountLoginWebViewActivity;
import org.mozilla.accounts.sync.FirefoxAccountSyncClient;
import org.mozilla.accounts.sync.commands.SyncCollectionCallback;
import org.mozilla.gecko.sync.repositories.domain.BookmarkRecord;
import org.mozilla.gecko.sync.repositories.domain.HistoryRecord;
import org.mozilla.gecko.sync.repositories.domain.PasswordRecord;

import java.util.List;

public class AccountsExampleActivity extends AppCompatActivity {

    private static final String LOGTAG = "AccountsExampleActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Intent intent = new Intent(this, FirefoxAccountLoginWebViewActivity.class);
        intent.putExtra(FirefoxAccountLoginWebViewActivity.EXTRA_ACCOUNT_CONFIG, FirefoxAccountEndpointConfig.getProduction());
        startActivityForResult(intent, 10); // TODO: request code.
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == FirefoxAccountLoginWebViewActivity.RESULT_OK) {
            final FirefoxAccount account = new FirefoxAccountDevelopmentStore(this).loadFirefoxAccount();
            if (account == null) {
                Log.d("lol", "Nothing.");
            } else {
                Log.d("lol", account.uid);
                sync(account);
            }
        } else if (resultCode == FirefoxAccountLoginWebViewActivity.RESULT_CANCELED) {
            Log.d("lol", "User canceled login");
        } else {
            Log.d("lol", "error!");
        }
    }

    private void sync(final FirefoxAccount account) {
        FirefoxAccountSyncClient client = new FirefoxAccountSyncClient(account);
        client.getPasswords(this, new SyncCollectionCallback<PasswordRecord>() {
            @Override
            public void onReceive(final List<PasswordRecord> receivedRecords) {
                Log.e(LOGTAG, "onReceive: passwords!");
                for (final PasswordRecord record : receivedRecords) {
                    Log.d(LOGTAG, record.encryptedPassword + ": " + record.encryptedUsername);
                }
            }

            @Override public void onError(final Exception e) { Log.e(LOGTAG, "onError: error!", e); }
        });
    }
}
