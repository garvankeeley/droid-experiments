package com.example.gkeeley.tableview_android;

import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainTable extends ListFragment implements AdapterView.OnItemClickListener {
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_fragment, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(createListAdapter());
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getActivity(), "Item: " + position, Toast.LENGTH_SHORT).show();
    }


    private List<Map<String, String>> convertToListItems(List<String> items) {
        final List<Map<String, String>> listItem = new ArrayList<>(items.size());
        for (String item : items) {
            Map<String, String> m = new HashMap<>();
            m.put("title-key", item);
            m.put("subtitle-key", "more info");
            listItem.add(Collections.unmodifiableMap(m));
        }
        return Collections.unmodifiableList(listItem);
    }

    private ListAdapter createListAdapter() {
        final String[] fromMapKey = new String[] {"title-key", "subtitle-key"};
        final int[] toLayoutId = new int[] {android.R.id.text1, android.R.id.text2};
        final List<Map<String, String>> list =
                convertToListItems(Arrays.asList(getResources().getStringArray(R.array.Planets)));

        return new SimpleAdapter(getActivity(),
                list,
                android.R.layout.simple_list_item_2,
                fromMapKey,
                toLayoutId);
    }
}