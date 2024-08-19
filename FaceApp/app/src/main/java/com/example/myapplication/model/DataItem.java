package com.example.myapplication.model;

import com.example.myapplication.activity.LoginActivity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class DataItem implements Serializable {
    private  int image;
    private String name;
    private String description;
    private String name_en;

    public String getName_en() {
        return name_en;
    }

    public void setName_en(String name_en) {
        this.name_en = name_en;
    }

    private String classLabel;

    public static final ArrayList<DataItem> allFishes = new ArrayList<>( Arrays.asList(
            new DataItem(1, "name","description","label","name english")
    ));

    public DataItem() {
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClassLabel() { return this.classLabel; }

    @Override
    public String toString() {
        return "Fish_Item{" +
                "image=" + image +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", classLabel='" + classLabel + '\'' +
                '}';
    }

    public DataItem(int image, String name, String description, String classLabel, String name_en) {
        this.image = image;
        this.name = name;
        this.description = description;
        this.classLabel = classLabel;
        this.name_en = name_en;
    }

    public boolean isLabel(String label) {
        return this.classLabel.equals(label);
    }

    public static String[] getClassLabelList(ArrayList<DataItem> fishItems) {
        ArrayList<String> labels = new ArrayList<>();
        for (DataItem fish : fishItems) {
            String label = fish.getClassLabel();
            if (!labels.contains(label)) {
                labels.add(label);
            }
        }
        String[] result = new String[labels.size()];
        result = labels.toArray(result);
        return result;
    }

    public boolean isFavourite() {
        if (LoginActivity.currentUser == null)
            return false;
        return LoginActivity.currentUser.getFavouriteFishes().contains(this.classLabel);
    }

    public static DataItem getFishByLabel(String label) {
        for (DataItem fish : allFishes) {
            if (fish.classLabel.equals(label))
                return fish;
        }
        return null;
    }
}
