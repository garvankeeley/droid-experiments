package com.example.gkeeley.tableview_android.recycler;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * A simple view holder to be used for a list of items.  To
 * keep things simple, just one TextView will be used.
 */
public class ItemViewHolder extends RecyclerView.ViewHolder {

    public TextView title;
    public TextView subtitle;

    public ItemViewHolder(View itemView) {
        super(itemView);
        this.title = (TextView) itemView.findViewById(android.R.id.text1);
        this.subtitle = (TextView) itemView.findViewById(android.R.id.text2);
    }


}