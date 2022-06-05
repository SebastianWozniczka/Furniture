package com.example.furniture;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ActivityLatest extends Activity  {


    @BindView(R.id.txt2) TextView txtCnt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_latest);
        Slidr.attach(this);
        ButterKnife.bind(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        getLatest();
    }

    private void getLatest() {
        for(int i=0; i<MainActivity.latest.size();i++) {
            String s=MainActivity.latest.get(i);
            String s2=s.replace("[","");
            String s3=s2.replace("]","");
            txtCnt.setText(s3);
        }
    }


}