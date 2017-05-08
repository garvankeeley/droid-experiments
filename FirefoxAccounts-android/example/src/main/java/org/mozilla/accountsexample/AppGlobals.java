package org.mozilla.accountsexample;

import org.mozilla.gecko.sync.repositories.domain.PasswordRecord;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AppGlobals {

        private static final AtomicInteger counter = new AtomicInteger();
        public static int uniqueIntForApp() {
            return counter.getAndIncrement();
        }

        public static Map<String, String> passwordRecordToMap(PasswordRecord record) {
            Map<String, String> map = new HashMap<>();
            map.put("id", record.id);
            map.put("hostname", record.hostname);
            map.put("username", record.encryptedUsername);
            map.put("password", record.encryptedPassword);
            return map;
        }

}
