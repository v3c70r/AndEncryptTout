package com.gu.tsing.andencrypttout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import org.openintents.openpgp.OpenPgpDecryptionResult;
import org.openintents.openpgp.OpenPgpError;
import org.openintents.openpgp.OpenPgpSignatureResult;
import org.openintents.openpgp.util.OpenPgpApi;
import org.openintents.openpgp.util.OpenPgpAppPreference;
import org.openintents.openpgp.util.OpenPgpKeyPreference;
import org.openintents.openpgp.util.OpenPgpServiceConnection;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Manifest;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }
    public static final int REQUEST_CODE_DECRYPT_AND_VERIFY = 9913;

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
        String providerPackageName = settings.getString("openpgp_provider_list", "");
        mSignKeyId = settings.getLong("openpgp_key", 0);
        pwdFilePath = settings.getString("pwd_selector","");

        if (TextUtils.isEmpty(providerPackageName) || mSignKeyId==0 || TextUtils.isEmpty(pwdFilePath)) {
            Toast.makeText(getContext(), "No OpenPGP app, key or pwd file selected!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getActivity(), SettingsActivity.class));
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

            //Permission check
            if (getActivity().checkSelfPermission(getActivity(), Manifest.permission.MANAGE_DOCUMETNS))
            //Decrypt file
            Intent decryptIntent = new Intent();
            decryptIntent.setAction(OpenPgpApi.ACTION_DECRYPT_VERIFY);
            InputStream is;

            try{
                is = getContext().getContentResolver().openInputStream(Uri.parse(pwdFilePath));
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                OpenPgpApi api = new OpenPgpApi(getActivity(), mServiceConnection.getService());
                api.executeApiAsync(decryptIntent, is, os, new MyCallback(false, os, REQUEST_CODE_DECRYPT_AND_VERIFY));
                is.close();
            } catch (FileNotFoundException e){
                showToast("File not found "+pwdFilePath);
                e.printStackTrace();
            }
            catch (IOException e){
                showToast("Some other errors");
                e.printStackTrace();
            }
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

    // =============OpenPgp Callbacks
    private class MyCallback implements OpenPgpApi.IOpenPgpCallback {
        boolean returnToCiphertextField;
        ByteArrayOutputStream os;
        int requestCode;

        private MyCallback(boolean returnToCiphertextField, ByteArrayOutputStream os, int requestCode) {
            this.returnToCiphertextField = returnToCiphertextField;
            this.os = os;
            this.requestCode = requestCode;

        }

        @Override
        public void onReturn(Intent result) {
            switch (result.getIntExtra(OpenPgpApi.RESULT_CODE, OpenPgpApi.RESULT_CODE_ERROR)) {
                case OpenPgpApi.RESULT_CODE_SUCCESS: {
                    showToast("RESULT_CODE_SUCCESS");

                    //// encrypt/decrypt/sign/verify
                    //if (os != null) {
                    //    try {
                    //        Log.d(OpenPgpApi.TAG, "result: " + os.toByteArray().length
                    //                + " str=" + os.toString("UTF-8"));

                    //        if (returnToCiphertextField) {
                    //            mCiphertext.setText(os.toString("UTF-8"));
                    //        } else {
                    //            mMessage.setText(os.toString("UTF-8"));
                    //        }
                    //    } catch (UnsupportedEncodingException e) {
                    //        Log.e(Constants.TAG, "UnsupportedEncodingException", e);
                    //    }
                    //}
                    switch (requestCode) {
                        case REQUEST_CODE_DECRYPT_AND_VERIFY: {
                            // RESULT_SIGNATURE and RESULT_DECRYPTION are never null!

                            OpenPgpSignatureResult signatureResult
                                    = result.getParcelableExtra(OpenPgpApi.RESULT_SIGNATURE);
                            showToast(signatureResult.toString());
                            OpenPgpDecryptionResult decryptionResult
                                    = result.getParcelableExtra(OpenPgpApi.RESULT_DECRYPTION);
                            showToast(decryptionResult.toString());

                            break;
                        }
                        default: {
                        }
                    }
                }
                case OpenPgpApi.RESULT_CODE_ERROR: {
                    showToast("RESULT_CODE_ERROR");

                    OpenPgpError error = result.getParcelableExtra(OpenPgpApi.RESULT_ERROR);
                    handleError(error);
                    break;
                }
            }
        }
    }
    private void handleError(final OpenPgpError error) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getActivity(),
                        "onError id:" + error.getErrorId() + "\n\n" + error.getMessage(),
                        Toast.LENGTH_LONG).show();
                Log.e(OpenPgpApi.TAG, "onError getErrorId:" + error.getErrorId());
                Log.e(OpenPgpApi.TAG, "onError getMessage:" + error.getMessage());
            }
        });
    }
    private void showToast(final String message) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(getActivity(),
                        message,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
