package com.example.richeditview;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 富文本编辑器
 * @Author pt
 * @Date 2022/11/15 10:15
 */
public class RichView extends LinearLayout {
    //保存数据的集合
    private List<RichBean> list = new ArrayList<>();
    private int focusPosition; //光标的纵坐标
    private int focusSelectionIndex;//光标的横坐标

    public RichView(Context context) {
        this(context, null);
    }

    public RichView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RichView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public List<RichBean> getData() {
        return list;
    }

    private void init() {
        RichBean richBean = new RichBean(0, "");
        insertList(richBean);
    }

    /**
     * 添加到集合中并且同步到UI中
     *
     * @param richBean
     */
    public void insertList(RichBean richBean) {
        Log.d("position---", focusPosition + "   " + focusSelectionIndex);
        if (list.isEmpty()) {
            list.add(richBean);
            insertLayout(richBean);
            return;
        }
        int length = list.get(focusPosition).content.length();

        //判断是从文本中间插入，还是末尾
        if (focusSelectionIndex == 0) {
            //从当前item头部插入
            list.add(focusPosition, richBean);
            insertLayout(richBean);
        } else if (focusSelectionIndex == length) {
            //尾部插入
            if (focusPosition != list.size() - 1) {
                //中间文本末尾
                list.add(focusPosition + 1, richBean);
                insertLayout(richBean);
            } else {
                //文章末尾
                list.add(richBean);
                insertLayout(richBean);
                RichBean endRichData = new RichBean(0, "");
                list.add(endRichData);
                insertLayout(endRichData);
            }

        } else {
            //文本中间插入，拆分一段文本为两段
            RichBean item = list.get(focusPosition);
            RichBean clone = item.clone();
            String content = item.content;
            Log.d("position---", content + "   size:" + list.size());
            item.content = content.substring(0, focusSelectionIndex);
            clone.content = content.substring(focusSelectionIndex, content.length());

            //更新原本的view
            View childAt = getChildAt(focusPosition);
            if (childAt != null) {
                if (childAt instanceof LinearLayout) {
                    View editView = ((LinearLayout) childAt).getChildAt(0);
                    if (editView != null && editView instanceof RichEditText) {
                        ((RichEditText) editView).setText(getSpannableString(item.content));
                    }
                }
            }
            list.add(focusPosition + 1, richBean);
            insertLayout(richBean);
            list.add(focusPosition + 2, clone);
            insertLayout(clone);

        }

    }

    /**
     * 添加话题
     *
     * @param topic
     */
    public void addTopic(String topic) {
        //过滤掉#
        if (topic.contains("#")) {
            topic = topic.replace("#", "");
        }
        topic = "#" + topic + "#";
        View focusLayout = getChildAt(focusPosition);
        if (focusLayout != null && focusLayout instanceof LinearLayout) {
            View focusEdit = ((LinearLayout) focusLayout).getChildAt(0);
            if (focusEdit != null && focusEdit instanceof RichEditText) {


                RichEditText focusEditView = (RichEditText) focusEdit;
//                SpannableString spannable = new SpannableString( topic );
//                spannable.setSpan(), 0, topic.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                if (focusSelectionIndex <= focusEditView.getText().toString().length()) {
                    String content = focusEditView.getText().toString();
                    String substring = content.substring(0, focusSelectionIndex);
                    String substringEnd = content.substring(focusSelectionIndex, content.length());
                    focusEditView.setText(substring);
                    topic = topic + substringEnd;
                    focusEditView.append(topic);
                }

            }
        }
    }

    /**
     * 查找所有的话题
     *
     * @param content
     * @return
     */
    public List<TopicBean> findAllTopic(String content) {
        int startIndex = 0;
        int endIndex = 0;
        List<TopicBean> list = new ArrayList<>();
        if (!TextUtils.isEmpty(content) && content.contains("#")) {
            while (startIndex != -1 && endIndex != -1) {
                if (startIndex != 0 || endIndex != 0) {
                    TopicBean topicBean = new TopicBean();
                    topicBean.start = startIndex;
                    topicBean.end = endIndex;
                    list.add(topicBean);
                }

                startIndex = content.indexOf("#", endIndex);
                endIndex = content.indexOf("#", startIndex + 1);
                if (endIndex != -1)
                    endIndex = endIndex == 0 ? 0 : endIndex + 1;
            }
        }
        return list;

    }

    /**
     * 插入到布局
     *
     * @param richBean
     */
    public void insertLayout(RichBean richBean) {
        int index = list.indexOf(richBean);
        if (index == -1) {
            return;
        }
        if (richBean.type == 0) {
            View richTextViewLayout = LayoutInflater.from(getContext()).inflate(R.layout.item_rich_text, this, false);
            RichEditText richEditText = richTextViewLayout.findViewById(R.id.edit);
            richEditText.setText(getSpannableString(richBean.content));
            richEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    Log.d("beforeTextChanged:", "start:" + start + "   after:" + after + "   count:" + count + "   focusSelectionIndex:" + focusSelectionIndex);
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    //处理话题的显示
                    int i = indexOfChild(richTextViewLayout);
                    if (s.toString().equals(list.get(i).content)) {
                        //字符串没有发生改变
                        return;
                    }
                    list.get(i).content = s.toString();
                    SpannableString spannableString1 = getSpannableString(s.toString());
                    richEditText.setText(spannableString1);

                }
            });
            //插入edittext
            richEditText.addOnSelectionChanged(new RichEditText.OnSelectionChanged() {
                @Override
                public void onSelectionChanged(int selStart, int selEnd) {
                    focusSelectionIndex = selStart;
                    focusPosition = list.indexOf(richBean);
                }
            });
            richEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        focusPosition = list.indexOf(richBean);
                        focusSelectionIndex = richEditText.getSelectionStart();
                    }

                }
            });

            richEditText.setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent keyEvent) {
                    if (keyCode == KeyEvent.KEYCODE_DEL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                        if (richEditText.getSelectionStart() == 0) {
                            //删除到头了
                            //移除本行
                            int index = indexOfChild(richTextViewLayout);
                            if (index == 0) return false;
                            View preChild = getChildAt(index - 1);
                            if (preChild != null && preChild instanceof LinearLayout) {
                                View preRichEdit = ((LinearLayout) preChild).getChildAt(0);
                                if (preRichEdit instanceof RichEditText) {
                                    int length = ((RichEditText) preRichEdit).getText().toString().length();
                                    if (!TextUtils.isEmpty(richEditText.getText())) {
                                        ((RichEditText) preRichEdit).append(richEditText.getText().toString());
                                    }
                                    ((RichEditText) preRichEdit).requestFocus();
                                    SoftKeyBoardUtil.showKeyboard(preRichEdit);
                                    ((RichEditText) preRichEdit).setSelection(length);
                                    list.remove(index);
                                    removeView(richTextViewLayout);
                                    invalidateFocus(index, true);
                                }

                            }

                            return true;
                        }
                        return false;
                    }
                    return false;
                }
            });
            if (index == list.size() - 1 && richEditText.getText().toString().isEmpty()) {
                //添加到最后的输入框，自动获取焦点
                richEditText.requestFocus();
            }
            addView(richTextViewLayout, index);
        } else if (richBean.type == 1) {
            //插入img
            View richTextViewLayout = LayoutInflater.from(getContext()).inflate(R.layout.item_rich_img, this, false);
            ImageView img = richTextViewLayout.findViewById(R.id.img);
            View imgDelete = richTextViewLayout.findViewById(R.id.img_delete);
            ViewGroup.LayoutParams viewLayoutParams = img.getLayoutParams();
            viewLayoutParams.width = richBean.width;
            viewLayoutParams.height = richBean.height;
            img.setLayoutParams(viewLayoutParams);
            Glide.with(getContext())
                    .load(richBean.content)
                    .into(img);
            addView(richTextViewLayout, index);
            invalidateFocus(index, false);
            imgDelete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteView(richTextViewLayout);
                }
            });
        } else if (richBean.type == 2) {
            //插入视频
            View richTextViewLayout = LayoutInflater.from(getContext()).inflate(R.layout.item_rich_video, this, false);
            ImageView img = richTextViewLayout.findViewById(R.id.video);
            View imgDelete = richTextViewLayout.findViewById(R.id.img_delete);
            ViewGroup.LayoutParams viewLayoutParams = img.getLayoutParams();
            viewLayoutParams.width = richBean.width;
            viewLayoutParams.height = richBean.height;
            img.setLayoutParams(viewLayoutParams);
            Glide.with(getContext())
                    .load(richBean.content)
                    .into(img);
            addView(richTextViewLayout, index);
            invalidateFocus(index, false);
            imgDelete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteView(richTextViewLayout);
                }
            });
        }
    }

    /**
     * string转--》带话题的spannableString
     *
     * @param content 普通文本
     * @return SpannableString 带话题样式的SpannableString
     */
    private SpannableString getSpannableString(String content) {
        List<TopicBean> allTopic = findAllTopic(content);
        SpannableString spannableString = new SpannableString(content);
        if (allTopic.size() > 0) {
            for (TopicBean topicBean : allTopic) {
                ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.parseColor("#FFED6052"));
                spannableString.setSpan(foregroundColorSpan, topicBean.start, topicBean.end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            }
        }
        return spannableString;
    }

    /**
     * 删除一个view
     *
     * @param view
     */
    private void deleteView(View view) {
        int i = indexOfChild(view);
        removeView(view);
        list.remove(i);
        //同步光标的位置，position位置发生变化
        invalidateFocus(i, true);
    }

    /**
     * 同步光标的纵坐标
     *
     * @param i 被移除的position or 被添加的position
     */
    private void invalidateFocus(int i, boolean isDelete) {
        if (focusPosition >= i) {
            if (isDelete) {
                focusPosition--;
            } else {
                focusPosition++;
            }

        }
    }
}
