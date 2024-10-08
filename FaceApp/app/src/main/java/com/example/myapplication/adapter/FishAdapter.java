package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.activity.DetailActivity;
import com.example.myapplication.activity.LoginActivity;
import com.example.myapplication.model.DataItem;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FishAdapter extends RecyclerView.Adapter<FishAdapter.FishViewHolder> implements Filterable {
    private List<DataItem> mListFish;
    private List<DataItem> mListFishOld;
    private Context mContext;

    public FishAdapter(Context context,List<DataItem> mListFish){
        this.mContext = context;
        this.mListFish = mListFish;
        this.mListFishOld = mListFish;
    }

    @NonNull
    @Override
    public FishViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_data,parent,false);
        return new FishViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FishViewHolder holder, int position) {
        DataItem fish = mListFish.get(position);
        if(fish.getClassLabel().equals("none")){
            return;
        }
        if(fish == null){
            return;
        }
        holder.imgFish.setImageResource(fish.getImage());
        holder.Name.setText(fish.getName());
        holder.Price.setText(fish.getDescription());
        holder.Name_en.setText(fish.getName_en());

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickGoToDetail(mContext, fish);
            }
        });

        if (fish.isFavourite())
            holder.addToFavouriteButton.setImageResource(R.drawable.ic_baseline_favorite_24);
        else
            holder.addToFavouriteButton.setImageResource(R.drawable.ic_baseline_shadow_favorite_24);
    }

    public static void onClickGoToDetail(Context mContext, DataItem fish_item) {
        if (fish_item != null) {
            Intent intent = new Intent(mContext, DetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("object_fish",fish_item);
            intent.putExtras(bundle);

            // Add fish label to viewed fish items of current User
            if (LoginActivity.currentUser != null)
                LoginActivity.currentUser.addViewedFish(fish_item.getClassLabel());
            else
                Log.d("FishAdapter", "currentUser is null");

            // Update new data to database
            if (LoginActivity.currentUserReference != null)
                LoginActivity.currentUserReference.setValue(LoginActivity.currentUser);
            else
                Log.d("FishAdapter", "currentUserReference is null");
            mContext.startActivity(intent);
        }
    }

    @Override
    public int getItemCount() {
        if(mListFish != null){
            return mListFish.size();
        }
        return 0;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String strSearch = constraint.toString();
                if(strSearch.isEmpty()){
                    mListFish = mListFishOld;
                }else {
                    List<DataItem> list = new ArrayList<>();
                    for(DataItem x : mListFishOld){
                        if(x.getName().toLowerCase().contains(strSearch.toLowerCase())){
                            list.add(x);
                        }
                    }
                    mListFish = list;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = mListFish;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mListFish = (List<DataItem>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public class FishViewHolder extends RecyclerView.ViewHolder{
        private androidx.cardview.widget.CardView cardView;
        private CircleImageView imgFish;
        private TextView Name;
        private TextView Price;
        private TextView Name_en;
        private ImageButton addToFavouriteButton;

        public FishViewHolder (@NonNull View itemView){
            super(itemView);
            imgFish = itemView.findViewById(R.id.img_fish);
            Name = itemView.findViewById(R.id.name_fish);
            Price = itemView.findViewById(R.id.price_fish);
            cardView = itemView.findViewById(R.id.card_fish);
            Name_en = itemView.findViewById(R.id.name_en_fish);
            addToFavouriteButton = itemView.findViewById(R.id.favBtn);

            addToFavouriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAbsoluteAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        DataItem selectedFish = mListFish.get(position);

                        if (!selectedFish.isFavourite()) {
                            LoginActivity.currentUser.addFavouriteFish(selectedFish.getClassLabel());
                            addToFavouriteButton.setImageResource(R.drawable.ic_baseline_favorite_24);
                        } else {
                            LoginActivity.currentUser.removeFavouriteFish(selectedFish.getClassLabel());
                            addToFavouriteButton.setImageResource(R.drawable.ic_baseline_shadow_favorite_24);
                        }

                        LoginActivity.currentUserReference.setValue(LoginActivity.currentUser);
                    }
                }
            });
        }

    }
}