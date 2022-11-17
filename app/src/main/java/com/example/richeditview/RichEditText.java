package com.example.richeditview;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

/**
 * @Description 输入框
 * @Author pt
 * @Date 2022/11/11 16:14
 */
public class RichEditText extends androidx.appcompat.widget.AppCompatEditText {
    private OnSelectionChanged onSelectionChanged;

    public RichEditText(Context context) {
        super(context);
    }

    public RichEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RichEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (onSelectionChanged!=null){
            onSelectionChanged.onSelectionChanged(selStart,selEnd);
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        //处理setText后光标位置为0的问题
        int selectionStart = getSelectionStart();
        super.setText(text, type);
        selectionStart=selectionStart==-1?0:selectionStart;
        setSelection(selectionStart);
        Log.d("selectionStart",selectionStart+" ");
    }

    @Override
    public void append(CharSequence text, int start, int end) {
        int selectionStart = getSelectionStart();
        super.append(text, start, end);
        //处理添加话题后光标位置后移
        if (text.toString().startsWith("#")){
            int i = text.toString().indexOf("#", 1);
            if (i!=-1){
                setSelection(selectionStart+i+1);
            }
        }
    }

    public void addOnSelectionChanged(OnSelectionChanged onSelectionChanged){
        this.onSelectionChanged=onSelectionChanged;
    }

    public interface OnSelectionChanged{
        void onSelectionChanged(int selStart,int selEnd);
    }

}
