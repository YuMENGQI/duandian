package com.permissionx.wheelview1;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    FromToTimePicker fromToTimePicker;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView= (TextView) findViewById(R.id.result);
        fromToTimePicker= (FromToTimePicker) findViewById(R.id.timePicker);
        fromToTimePicker.setOnResultListener(new FromToTimePicker.OnResultListener() {
            @Override
            public void onConfirm( int fromHour, int fromMinute, int toHour, int toMinute) {
                textView.setText("From "+fromHour+":"+fromMinute+" To "+toHour+":"+toMinute);
            }
            @Override
            public void onCancel() {
                textView.setText("Canceled");
            }
        });
        fromToTimePicker.setCurrentDate(TimeUtil.getTimeString(),TimeUtil.addTime(TimeUtil.getTimeString(),"8:00").getTime());

    }
}
