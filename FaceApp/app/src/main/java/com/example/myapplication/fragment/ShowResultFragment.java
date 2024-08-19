package com.example.myapplication.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.adapter.FishAdapter;
import com.example.myapplication.model.DataItem;

import java.util.Stack;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowResultFragment extends Fragment {
    Bitmap selectedImage;
    Bitmap maskImage;
    Bitmap heatmapImage;
    DataItem predictedFish;
    String confidence;

    View view;

    ImageView selectedImageView, maskImageView, heatmapImageView;
    CircleImageView predictedFishImageView;
    TextView predictedFishName;
    TextView predictedFishPrice;
    TextView predictedFishNameEn;
//    TextView confidenceTextView;
    CardView predictedFishCardView;

    Stack<Bitmap> imageStack = new Stack<>();
    Stack<Pair<DataItem, String>> resultStack = new Stack<>();

    public ShowResultFragment() {
        super(R.layout.activity_show_result);
    }

    public ShowResultFragment(Bitmap selectedImage, Bitmap maskImage, Bitmap heatmapImage) {
        super(R.layout.activity_show_result);
        this.selectedImage = selectedImage;
        this.maskImage = maskImage;
        this.heatmapImage = heatmapImage;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = super.onCreateView(inflater, container, savedInstanceState);

        if (view != null) {
            selectedImageView = view.findViewById(R.id.selected_image_view);
            maskImageView = view.findViewById(R.id.mask_image_view);
            heatmapImageView = view.findViewById(R.id.heatmap_image_view);

            selectedImageView.setImageBitmap(this.selectedImage);
            maskImageView.setImageBitmap(this.maskImage);
            heatmapImageView.setImageBitmap(this.heatmapImage);

            if (predictedFish != null) {
                predictedFishImageView.setImageBitmap (
                        BitmapFactory.decodeResource (
                                view.getContext().getResources(),
                                predictedFish.getImage()
                        )
                );
                predictedFishName.setText(predictedFish.getName());
                predictedFishPrice.setText(predictedFish.getDescription());
                predictedFishNameEn.setText(predictedFish.getName_en());
//                confidenceTextView.setText(confidence);

                if(!predictedFish.isLabel("none")){
                predictedFishCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FishAdapter.onClickGoToDetail(getContext(), predictedFish);
                    }
                });}
            }
        } else
            Log.d("ShowResultFragment", "onCreateView: view is null");

        return view;
    }
}
