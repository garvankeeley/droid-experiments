package com.example.gkeeley.tableview_android.recycler;

import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gkeeley.tableview_android.ClipboardDialog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * A simple RecyclerView.Adapter class that manages items.
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemViewHolder> {

    private List<ItemAdapter.Item> mItems;

    public static String keyTitle = "";
    public static String keySubtitle = "";

    public static class Item {
        Map<String, String> data = new HashMap<>();
    }

    public ItemAdapter(List<ItemAdapter.Item> items) {
        mItems = items;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        Item item = mItems.get(position);
        final String title = item.data.get(keyTitle);
        holder.title.setText(title);
        holder.subtitle.setText(item.data.get(keySubtitle));

        //apply on click on your root view
        final View root = holder.title.getRootView();
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardDialog.show("www.google.com", root.getContext());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }


}/**/