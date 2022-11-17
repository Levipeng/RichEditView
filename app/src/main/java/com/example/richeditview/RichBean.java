package com.example.richeditview;

import android.util.Log;

import androidx.annotation.NonNull;


/**
 * @Description 数据
 * @Author pt
 * @Date 2022/10/11 16:52
 */
public class RichBean implements Cloneable{
    public static final int ITEM_TYPE_TEXT = 0; // 纯文字
    public static final int ITEM_TYPE_IMG = 1; // 图片
    public static final int ITEM_TYPE_VIDEO = 2; //视频
    public int type;
    public String content;
    public int width;
    public int height;
    public RichBean(int type, String content) {
        this.type = type;
        this.content = content;
    }



    @NonNull
    @Override
    protected RichBean clone()  {
        RichBean richBean = null;
        try{
            richBean = (RichBean)super.clone();
        }catch(CloneNotSupportedException e) {
            Log.e("Exception",e.getMessage());
        }
        return richBean;
    }

}
