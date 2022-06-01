package com.example.mapwithmarker;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MarkersAdapter extends RecyclerView.Adapter<MarkersAdapter.CustomViewHolder>{
    private ArrayList<mMarker> mList = null;
    private Activity context = null;


    public MarkersAdapter(Activity context, ArrayList<mMarker> list) {
        this.context = context;
        this.mList = list;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected TextView id;
        protected TextView lat;
        protected TextView lon;


        public CustomViewHolder(View view) {
            super(view);
            this.id = (TextView) view.findViewById(R.id.textView_list_id);
            this.lat = (TextView) view.findViewById(R.id.textView_list_lat);
            this.lon = (TextView) view.findViewById(R.id.textView_list_lon);
        }
    }


    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder viewholder, int position) {

        viewholder.id.setText(mList.get(position).getId());
        viewholder.lat.setText(mList.get(position).getLat());
        viewholder.lon.setText(mList.get(position).getLon());
    }

    @Override
    public int getItemCount() {
        return (null != mList ? mList.size() : 0);
    }
}
