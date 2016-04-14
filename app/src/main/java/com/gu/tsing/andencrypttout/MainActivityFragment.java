package com.gu.tsing.andencrypttout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.openintents.openpgp.IOpenPgpService2;
import org.openintents.openpgp.util.OpenPgpApi;
import org.openintents.openpgp.util.OpenPgpAppPreference;
import org.openintents.openpgp.util.OpenPgpKeyPreference;
import org.openintents.openpgp.util.OpenPgpServiceConnection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    private ArrayAdapter<String> mPwdAdapter;
    private OpenPgpServiceConnection mServiceConnection;
    private long mSignKeyId;
    private String pwdFilePath;
    SharedPreferences settings;

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public  void onStart() {
        super.onStart();
        pwdFilePath = settings.getString("pwd_selector","");
        Toast.makeText(getActivity(), pwdFilePath + " Selected", Toast.LENGTH_SHORT).show();    //Debugging
        String providerPackageName = settings.getString("openpgp_provider_list", "");
        mSignKeyId = settings.getLong("openpgp_key", 0);
        pwdFilePath = settings.getString("pwd_selector","");

        if (TextUtils.isEmpty(providerPackageName)) {
            Toast.makeText(getContext(), "No OpenPGP app selected!", Toast.LENGTH_LONG).show();
            //getActivity().finish();
        } else if (mSignKeyId == 0) {
            Toast.makeText(getContext(), "No key selected!", Toast.LENGTH_LONG).show();
            //getActivity().finish();
        } else {
            // bind to service
            mServiceConnection = new OpenPgpServiceConnection(
                    getContext(),
                    providerPackageName,
                    new OpenPgpServiceConnection.OnBound() {
                        @Override
                        public void onBound(IOpenPgpService2 service) {
                            Log.d(OpenPgpApi.TAG, "onBound!");
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(OpenPgpApi.TAG, "exception when binding!", e);
                        }
                    }
            );
            mServiceConnection.bindToService();
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Init OpenGPG
        settings = PreferenceManager.getDefaultSharedPreferences(getContext());

        // Decrypt data


        // Mock data ======================
        String dataStr = "no\nthing\nis\nhere\noops\n";

        List<String> dataList = new ArrayList<String>(Arrays.asList(dataStr.split("\n")));
        mPwdAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_pwd,
                R.id.list_item_pwd_TextView,
                dataList);

        View rootView =  inflater.inflate(R.layout.fragment_main, container, false);
        final ListView listView = (ListView) rootView.findViewById(R.id.listView_pwd);
        listView.setAdapter(mPwdAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Toast.makeText(getActivity(), "Clicked on position "+Integer.toString(position) + " with row id "+Long.toString(l), Toast.LENGTH_SHORT).show();
            }
        });
        EditText searchEdit = (EditText)rootView.findViewById(R.id.editText_search);
        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.equals(""))
                    mPwdAdapter.getFilter().filter(s);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        return rootView;
    }
}
