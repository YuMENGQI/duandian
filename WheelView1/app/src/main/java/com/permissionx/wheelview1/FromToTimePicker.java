package com.permissionx.wheelview1;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;


public class FromToTimePicker extends LinearLayout {
    private WheelView mWheelFromHour;
    private WheelView mWheelFromMinute;
    private WheelView mWheelToHour;
    private WheelView mWheelToMinute;
    private Button mCancelBtn;
    private Button mConfirmBtn;
    private int mFromMinute;
    private int mFromHour;
    private int mToHour;
    private int mToMinute;

    private OnResultListener onResultListener;

    private WheelView.OnSelectListener mFromHourListener = new WheelView.OnSelectListener() {
        @Override
        public void endSelect(int hour, String text) {
            mFromHour = hour;
        }

        @Override
        public void selecting(int id, String text) {
            mFromHour = id;
        }
    };

    private WheelView.OnSelectListener mFromMinuteListener = new WheelView.OnSelectListener() {
        @Override
        public void endSelect(int minute, String text) {
            mFromMinute = minute;
        }

        @Override
        public void selecting(int id, String text) {
            mFromMinute = id;
        }
    };


    private WheelView.OnSelectListener mToHourListener = new WheelView.OnSelectListener() {
        @Override
        public void endSelect(int hour, String text) {
            mToHour = hour;
        }

        @Override
        public void selecting(int hour, String text) {
            mToHour = hour;
        }
    };
    private WheelView.OnSelectListener mToMinuteListener = new WheelView.OnSelectListener() {
        @Override
        public void endSelect(int minute, String text) {
            mToMinute = minute;
        }

        @Override
        public void selecting(int minute, String text) {
            mToMinute = minute;
        }
    };

    private Activity mContext;

    public FromToTimePicker(Context context) {
        this(context, null);
    }

    public FromToTimePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContext = (Activity) getContext();
        LayoutInflater.from(mContext).inflate(R.layout.time_picker_situation, this);
        mWheelFromHour = findViewById(R.id.from_hour);
        mWheelFromMinute = findViewById(R.id.from_minute);
        mWheelToHour = findViewById(R.id.to_hour);
        mWheelToMinute = findViewById(R.id.to_minute);


        mCancelBtn =  findViewById(R.id.cancel);
        mConfirmBtn = findViewById(R.id.confirm);
        mWheelFromHour.setOnSelectListener(mFromHourListener);
        mWheelFromMinute.setOnSelectListener(mFromMinuteListener);
        mWheelToHour.setOnSelectListener(mToHourListener);
        mWheelToMinute.setOnSelectListener(mToMinuteListener);
        mConfirmBtn.setOnClickListener(v -> {
            if (onResultListener != null) {
                onResultListener.onConfirm(mFromHour, mFromMinute, mToHour, mToMinute);
            }
        });
        mCancelBtn.setOnClickListener(v -> {
            if (onResultListener != null) {
                onResultListener.onCancel();
            }
        });
        setDate();
    }

    private ArrayList<String> getHourData() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            list.add(i < 10 ? "0" + i : i + "");
        }
        return list;
    }

    private ArrayList<String> getMinuteData() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i <= 59; i++) {
            list.add(i < 10 ? "0" + i : i + "");
        }
        return list;
    }

    public interface OnResultListener {
        void onConfirm(int fromHour, int fromMinute, int toHour, int toMinute);

        void onCancel();
    }

    private void setDate() {
        mWheelFromHour.setData(getHourData());
        mWheelFromMinute.setData(getMinuteData());
        mWheelToHour.setData(getHourData());
        mWheelToMinute.setData(getMinuteData());
    }

    public void setCurrentDate(String from, String to) {
        // 从外面设置当前的时间进来

        mFromHour = TimeUtil.getHourFromTime(from);
        mFromMinute = TimeUtil.getMinuteFromTime(from);
        mToHour = TimeUtil.getHourFromTime(to);
        mToMinute = TimeUtil.getMinuteFromTime(to);
        mWheelFromHour.setDefault(mFromHour);
        mWheelFromMinute.setDefault(mFromMinute);
        mWheelToHour.setDefault(mToHour);
        mWheelToMinute.setDefault(mToMinute);
    }

    public void setOnResultListener(OnResultListener onResultListener) {
        this.onResultListener = onResultListener;
    }
}