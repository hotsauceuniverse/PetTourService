package com.seyoung.pettourservice;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TourTypeAdapter extends RecyclerView.Adapter<TourTypeAdapter.ViewHolder> {

    private List<TourTypeData> mTourTypeData;

    public TourTypeAdapter(List<TourTypeData> tourTypeData) {
        mTourTypeData = tourTypeData;
    }

    @NonNull
    @Override
    public TourTypeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.tour_recyclerview, parent, false);
        ViewHolder viewHolder = new ViewHolder(linearLayout);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TourTypeAdapter.ViewHolder holder, int position) {
        if (mTourTypeData != null && !mTourTypeData.isEmpty()) {
            TourTypeData tourTypeData = mTourTypeData.get(position);
            holder.title_tv.setText(tourTypeData.getTitle());
            holder.tour_type_tv.setText(tourTypeData.getTourType());
            holder.address_tv.setText(tourTypeData.getAddress());
        }
    }

    @Override
    public int getItemCount() {
        return mTourTypeData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title_tv;
        public TextView tour_type_tv;
        public TextView address_tv;
        public TextView noData;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title_tv = itemView.findViewById(R.id.title_tv);
            tour_type_tv = itemView.findViewById(R.id.tour_type_tv);
            address_tv = itemView.findViewById(R.id.addr_tv);
            noData = itemView.findViewById(R.id.no_data);
        }
    }
}


//데이터 없을때 데이터 없음 텍스트 출력