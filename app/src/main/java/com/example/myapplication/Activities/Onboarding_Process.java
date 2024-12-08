package com.example.myapplication.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import com.example.myapplication.Utilities.DepthPageTransformer;
import com.example.myapplication.Adapters.OnboardingPagerAdapter;
import com.example.myapplication.R;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

public class Onboarding_Process extends AppCompatActivity {

    Intent dash;
    WormDotsIndicator dots;
    ImageButton back_btn;
    AppCompatButton skip_btn, next_btn;
    ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_process);

        back_btn = findViewById(R.id.back_btn);
        next_btn = findViewById(R.id.next_btn);
        skip_btn = findViewById(R.id.skip_btn);

        dash = new Intent(Onboarding_Process.this, Dash_Board.class);
        dash.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

        viewPager = findViewById(R.id.viewPager_onboarding);
        OnboardingPagerAdapter adapter = new OnboardingPagerAdapter(this);
        viewPager.setAdapter(adapter);


        dots = findViewById(R.id.dots);
        dots.attachTo(viewPager);           //attaching warm dot indicator to the viewpager using a third party library

        viewPager.setPageTransformer(new DepthPageTransformer());    //applying depth page transistion animation while sliding in the viewpager

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);

            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if(position > 0){
                    back_btn.setVisibility(View.VISIBLE);
                    skip_btn.setVisibility(View.VISIBLE);
                    if(position == 2)
                        skip_btn.setVisibility(View.INVISIBLE);
                } else{
                    skip_btn.setVisibility(View.VISIBLE);
                    back_btn.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

        back_btn.setOnClickListener(v -> {
            if(getItem(0) > 0){
                viewPager.setCurrentItem(getItem(-1), true);
            }
        });

        next_btn.setOnClickListener(v -> {
            if(getItem(0) < 2){
                viewPager.setCurrentItem(getItem(1), true);
            } else{
                startActivity(dash);
            }
        });

        skip_btn.setOnClickListener(v -> startActivity(dash));



    }

    private int getItem(int i){
        return (viewPager.getCurrentItem() + i);
    }

    @Override
    public void onBackPressed() {
        if(getItem(0)>0){
            viewPager.setCurrentItem(getItem(0)-1);
        } else{
            super.onBackPressed();
        }
    }
}