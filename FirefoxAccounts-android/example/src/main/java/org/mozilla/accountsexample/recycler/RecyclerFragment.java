package org.mozilla.accountsexample.recycler;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.mozilla.accountsexample.R;
import java.util.ArrayList;
import java.util.List;

public class RecyclerFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<ItemAdapter.Item> items = new ArrayList<>();
    private ItemAdapter itemAdapter;

    public RecyclerFragment() {
        ItemAdapter.keyTitle = "site";
        ItemAdapter.keySubtitle = "username";
    }

    public static RecyclerFragment newInstance(/* params in future*/) {
        RecyclerFragment fragment = new RecyclerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler, container, false);
        recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        itemAdapter = new ItemAdapter(items);
        recyclerView.setAdapter(itemAdapter);
        loadData();
        return view;
    }

    private void loadData() {
        //Add some test items - enough to cause the recycler to be used
        for(int i = 0; i < 10000; ++i) {
            ItemAdapter.Item item = new ItemAdapter.Item();
            item.data.put("site", "www.google.ca");
            item.data.put("username", "garvankeeley@gmail.com");
            items.add(item);
        }
    }


}
