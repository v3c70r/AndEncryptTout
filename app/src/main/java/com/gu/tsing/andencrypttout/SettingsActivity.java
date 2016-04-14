/*
 * Copyright (C) 2012-2014 Dominik Schürmann <dominik@dominikschuermann.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gu.tsing.andencrypttout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

import org.openintents.openpgp.util.OpenPgpAppPreference;
import org.openintents.openpgp.util.OpenPgpKeyPreference;

public class SettingsActivity extends PreferenceActivity {
    OpenPgpKeyPreference mKey;
    OpenPgpAppPreference mProvider;

    private final int SELECTOR_CODE=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // load preferences from xml
        addPreferencesFromResource(R.xml.base_preference);

        // find preferences
        //Preference openKeychainIntents = findPreference("intent_demo");
        //Preference openPgpApi = findPreference("openpgp_provider_demo");

        mProvider = (OpenPgpAppPreference) findPreference("openpgp_provider_list");
        mKey = (OpenPgpKeyPreference) findPreference("openpgp_key");
        mKey.setOpenPgpProvider(mProvider.getValue());
        mProvider.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                mKey.setOpenPgpProvider((String) newValue);
                return true;
            }
        });
        mKey.setDefaultUserId("Alice <alice@example.com>");

        Preference filePicker = (Preference) findPreference("pwd_selector");
        filePicker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent selectFileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                selectFileIntent.setType("*/*");
                startActivityForResult(selectFileIntent, SELECTOR_CODE);
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mKey.handleOnActivityResult(requestCode, resultCode, data)) {
            // handled by OpenPgpKeyPreference
            return;
        }
        switch (requestCode) {
            case SELECTOR_CODE:
                if (resultCode == RESULT_OK) {
                    String Fpath = data.getDataString();
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("pwd_selector", Fpath);
                    editor.commit();
                    Toast.makeText(SettingsActivity.this, Fpath + " Selected", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        // other request codes...
    }
}