package com.example.myapplication.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.helper.LocaleHelper;

public class ChooseLanguageFragment extends Fragment {
    public ChooseLanguageFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocaleHelper.showChangeLanguageDialog(
                this.getContext(), this.getActivity()
        );
    }
}
