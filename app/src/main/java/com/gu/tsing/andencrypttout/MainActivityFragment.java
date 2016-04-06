package com.gu.tsing.andencrypttout;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Mock data ======================
        String[] data = {
                "Today - Sunny - 10/20",
                "Tomorrow - Sunny - 10/20",
                "Weds - Sunny - 10/20",
                "Thurs - Sunny - 10/20",
                "Fri - Sunny - 10/20",
                "Sat - Sunny - 10/20",
                "Fri - Sunny - 10/20",
                "Fri - Sunny - 10/20",
                "Fri - Sunny - 10/20",
                "Fri - Sunny - 10/20",
                "Fri - Sunny - 10/20",
                "Fri - Sunny - 10/20",
                "Fri - Sunny - 10/20",
                "Fri - Sunny - 10/20"
        };
        List<String> dataList = new ArrayList<String>(Arrays.asList(data));
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
