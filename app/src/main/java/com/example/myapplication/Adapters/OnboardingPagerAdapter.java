package com.example.myapplication.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.myapplication.Fragments.onboardingFragment1;
import com.example.myapplication.Fragments.onboardingFragment2;
import com.example.myapplication.Fragments.onboardingFragment3;

public class OnboardingPagerAdapter extends FragmentStateAdapter {

    public OnboardingPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(position == 0){
            return new onboardingFragment1();
        } else if(position == 1){
            return new onboardingFragment2();
        } else{
            return new onboardingFragment3();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
