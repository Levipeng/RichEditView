package com.example.richeditview;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private TextView mTxtContent;
    private RichView mRichView;
    private ScrollView scrollLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTxtContent = findViewById(R.id.txt_content);
        mRichView = findViewById(R.id.rich_view);
        scrollLayout=findViewById(R.id.scroll_layout);
    }

    public void selectImg(View view) {
        openAlbum();
    }

    public void selectVideo(View view) {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 66);

    }

    public void selectTopic(View view) {
        mRichView.addTopic("#哈哈哈#");
    }

    public void showContent(View view) {
        String text = "";
        for (RichBean data : mRichView.getData()) {
            text = text + "\n\n\n" + data.content;
        }
        mTxtContent.setText(text);
        if (scrollLayout.getVisibility() == View.VISIBLE) {
            scrollLayout.setVisibility(View.GONE);
            mTxtContent.setVisibility(View.VISIBLE);
        } else {
            scrollLayout.setVisibility(View.VISIBLE);
            mTxtContent.setVisibility(View.GONE);
        }
    }

    /**
     * 打开系统相册
     */
    public void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        //设置请求码，以便我们区分返回的数据
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (100 == requestCode) {
            if (data != null) {
                //获取数据
                //获取内容解析者对象
                try {
                    Bitmap mBitmap = BitmapFactory.decodeStream(
                            getContentResolver().openInputStream(data.getData()));
                    float v1 = (mBitmap.getHeight() * 1f) / mBitmap.getWidth();
                    RichBean richBean = new RichBean(1, data.getData().toString());
                    int screenWidth =getScreenWidth(getApplicationContext());
                    screenWidth = screenWidth - dp2px(getApplicationContext(), 10);

                    richBean.width = screenWidth;
                    richBean.height =(int) (screenWidth * v1);
                    mRichView.insertList(richBean);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

            }
        } else if (66 == requestCode) {
            Uri selectedVideo = data.getData();
            String[] filePathColumn = {MediaStore.Video.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedVideo,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String videoPath = cursor.getString(columnIndex);
            cursor.close();
            RichBean richBean = getVideo(videoPath);
            int width = richBean.width;
            int height = richBean.height;
            float v1 = (height * 1f) / width;
            int screenWidth = getScreenWidth(getApplicationContext());
            screenWidth = screenWidth - dp2px(getApplicationContext(), 10);
            richBean.width=screenWidth;
            richBean.height=(int) (screenWidth * v1);
            mRichView.insertList(richBean);

        }

    }


    private RichBean getVideo(String mUri) {
        RichBean richBean = new RichBean(2, mUri);


        android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();
        try {
            if (mUri != null) {
                mmr.setDataSource(mUri);
            } else {
                //mmr.setDataSource(mFD, mOffset, mLength);
            }

            String duration = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);//时长(毫秒)
            String width = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);//宽
            String height = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);//高
            richBean.width=Integer.parseInt(width);
            richBean.height=Integer.parseInt(height);

        } catch (Exception ex) {
            Log.e("TAG", "MediaMetadataRetriever exception " + ex);
        } finally {
            mmr.release();
        }
        return richBean;
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            DisplayMetrics outMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(outMetrics);
            return outMetrics.widthPixels;
        }
        return 0;
    }

    public static int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal,
                context.getResources().getDisplayMetrics());
    }
}