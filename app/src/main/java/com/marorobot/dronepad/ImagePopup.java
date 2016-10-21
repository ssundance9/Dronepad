package com.marorobot.dronepad;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * 상세 이미지 Activity
 */
public class ImagePopup extends AppCompatActivity implements View.OnClickListener {

    private Context mContext = null;
    private final int imgWidth = 700;
    private final int imgHeight = 700;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_popup);
        mContext = this;

        /** 전송메시지 */
        Intent i = getIntent();
        Bundle extras = i.getExtras();
        String imgPath = extras.getString("filename");

        /** 완성된 이미지 보여주기  */
        BitmapFactory.Options bfo = new BitmapFactory.Options();
        bfo.inSampleSize = 4;
        ImageView iv = (ImageView)findViewById(R.id.imageViewPop);
        Bitmap bm = BitmapFactory.decodeFile(imgPath, bfo);

        int height=bm.getHeight();
        int width=bm.getWidth();

        Bitmap resized = Bitmap.createScaledBitmap(bm, width, height, true);
        iv.setImageBitmap(resized);
    }

    public void onClick(View v) {

    }

    public void onBtnReturn(View view) {
        finish();
    }

    public void onBtnClose(View view) {
        finish();
    }
}
