package com.marorobot.dronepad;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class Photo2Activity extends AppCompatActivity {

    private Context mContext;
    private final int listSize = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo2);
        mContext = this;
        
        //HorizontalScrollView mScrollView = (HorizontalScrollView) findViewById(R.id.photo_horizontablscrollview);
        LinearLayout ll = (LinearLayout) findViewById(R.id.photo_linearLayout);
        
        //ImageAdapter ia = new ImageAdapter(this);
        //int totalCountImage = ia.getCount();
        //int totalPageSize = totalCountImage / listSize + 1;
        int totalCountImage = this.getTotalCountImage();

        Log.e("aaaaaaaa", String.valueOf(totalCountImage));

        int totalPageSize = totalCountImage / listSize;
        int a = totalCountImage % listSize;
        if (a != 0) {
            totalPageSize = totalPageSize + 1;
        }

        //for (int i = 1; i <= totalPageSize; i++) {
        for (int i = 1; i <= 2; i++) {

            final ImageAdapter tempIA = new ImageAdapter(this, i);
            GridView gv = getNewGridView();
            gv.setAdapter(tempIA);
            gv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                public void onItemClick(AdapterView parent, View v, int position, long id){
                    tempIA.callImageViewer(position);
                }
            });
            
            ll.addView(gv);
        }
    }
        
    private GridView getNewGridView() {
        GridView gv = new GridView(this);
        // set gridview attr
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT
                                                                        , ViewGroup.LayoutParams.WRAP_CONTENT);
        gv.setLayoutParams(params);
        gv.setGravity(Gravity.CENTER);
        gv.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        gv.setColumnWidth(96);
        gv.setHorizontalSpacing(5);
        gv.setVerticalSpacing(5);
        gv.setNumColumns(4);
        
        return gv;
    }

    private int getTotalCountImage() {
        String[] proj = {MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE};

        Cursor imageCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                proj, null, null, null);

        return imageCursor.getCount();
    }


    
    

    /**==========================================
     *              Adapter class
     * ==========================================*/
    public class ImageAdapter extends BaseAdapter {
        private String imgData;
        private String geoData;
        private ArrayList<String> thumbsDataList;
        private ArrayList<String> thumbsIDList;

        /*ImageAdapter(Context c){
            mContext = c;
            thumbsDataList = new ArrayList<String>();
            thumbsIDList = new ArrayList<String>();
            getThumbInfo(thumbsIDList, thumbsDataList);
        }*/
        
        // startNum ~ startNum + 8, startNum = -1 then lastPage
        ImageAdapter(Context c, int pageNum){
            mContext = c;
            thumbsDataList = new ArrayList<String>();
            thumbsIDList = new ArrayList<String>();
            getThumbInfo(thumbsIDList, thumbsDataList, pageNum);
        }

        public final void callImageViewer(int selectedIndex){
            Intent i = new Intent(mContext, ImagePopup.class);
            String imgPath = getImageInfo(imgData, geoData, thumbsIDList.get(selectedIndex));
            i.putExtra("filename", imgPath);
            startActivityForResult(i, 1);
        }

        public boolean deleteSelected(int sIndex){
            return true;
        }

        public int getCount() {
            return thumbsIDList.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null){
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(180, 180));
                imageView.setAdjustViewBounds(false);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(2, 2, 2, 2);
            }else{
                imageView = (ImageView) convertView;
            }
            BitmapFactory.Options bo = new BitmapFactory.Options();
            bo.inSampleSize = 8;
            Bitmap bmp = BitmapFactory.decodeFile(thumbsDataList.get(position), bo);
            Bitmap resized = Bitmap.createScaledBitmap(bmp, 180, 180, true);
            imageView.setImageBitmap(resized);

            return imageView;
        }
        
        private void getThumbInfo(ArrayList<String> thumbsIDs, ArrayList<String> thumbsDatas, int pageNum){
            int pos = (pageNum - 1) * listSize;

            String[] proj = {MediaStore.Images.Thumbnails._ID,
                    MediaStore.Images.Thumbnails.DATA
                    //MediaStore.Images.Thumbnails.DISPLAY_NAME,
                    //MediaStore.Images.Thumbnails.SIZE
            };

            Cursor imageCursor = getContentResolver().query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                    proj, null, null, null);

            if (imageCursor != null && imageCursor.moveToPosition(pos)){
                String title;
                String thumbsID;
                String thumbsImageID;
                String thumbsData;
                String data;
                String imgSize;

                int thumbsIDCol = imageCursor.getColumnIndex(MediaStore.Images.Thumbnails._ID);
                int thumbsDataCol = imageCursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA);
                //int thumbsImageIDCol = imageCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                //int thumbsSizeCol = imageCursor.getColumnIndex(MediaStore.Images.Media.SIZE);
                int num = 0;
                do {
                    thumbsID = imageCursor.getString(thumbsIDCol);
                    thumbsData = imageCursor.getString(thumbsDataCol);
                    //thumbsImageID = imageCursor.getString(thumbsImageIDCol);
                    //imgSize = imageCursor.getString(thumbsSizeCol);
                    num++;
                    if (thumbsID != null){
                        thumbsIDs.add(thumbsID);
                        thumbsDatas.add(thumbsData);
                    }
                } while (imageCursor.moveToNext() && num < listSize);
            }
            imageCursor.close();
            return;
        }

        /*private void getThumbInfo(ArrayList<String> thumbsIDs, ArrayList<String> thumbsDatas){
            String[] proj = {MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.SIZE};

            Cursor imageCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    proj, null, null, null);

            if (imageCursor != null && imageCursor.moveToFirst()){
                String title;
                String thumbsID;
                String thumbsImageID;
                String thumbsData;
                String data;
                String imgSize;

                int thumbsIDCol = imageCursor.getColumnIndex(MediaStore.Images.Media._ID);
                int thumbsDataCol = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
                int thumbsImageIDCol = imageCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                int thumbsSizeCol = imageCursor.getColumnIndex(MediaStore.Images.Media.SIZE);
                int num = 0;
                do {
                    thumbsID = imageCursor.getString(thumbsIDCol);
                    thumbsData = imageCursor.getString(thumbsDataCol);
                    thumbsImageID = imageCursor.getString(thumbsImageIDCol);
                    imgSize = imageCursor.getString(thumbsSizeCol);
                    num++;
                    if (thumbsImageID != null){
                        thumbsIDs.add(thumbsID);
                        thumbsDatas.add(thumbsData);
                    }
                }while (imageCursor.moveToNext());
            }
            imageCursor.close();
            return;
        }*/

        private String getImageInfo(String ImageData, String Location, String thumbID){
            String imageDataPath = null;
            String[] proj = {MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.SIZE};
            Cursor imageCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    proj, "_ID='"+ thumbID +"'", null, null);

            if (imageCursor != null && imageCursor.moveToFirst()){
                if (imageCursor.getCount() > 0){
                    int imgData = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    imageDataPath = imageCursor.getString(imgData);
                }
            }
            imageCursor.close();
            return imageDataPath;
        }
    }

    public void onBtnReturn(View view) {
        Intent intent = new Intent(getApplicationContext(), IntroActivity.class);
        startActivity(intent);
    }
}
