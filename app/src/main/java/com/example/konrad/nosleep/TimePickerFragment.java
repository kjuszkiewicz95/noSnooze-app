package com.example.konrad.nosleep;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;

/**
 * Created by Konrad on 1/18/2016.
 */
public class TimePickerFragment extends DialogFragment{
    TimePicker mTimePicker;
    public static final String EXTRA_HOUR = "com.example.konrad.nosnooze.hour";
    public static final String EXTRA_MINUTE = "com.example.konrad.nosnooze.minute";
    private int mHour;
    private int mMinute;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_time, null);
        mTimePicker = (TimePicker)v.findViewById(R.id.timePicker);
        Bundle args = getArguments();
        if (args != null) {
            mHour = args.getInt(EXTRA_HOUR);
            mMinute = args.getInt(EXTRA_MINUTE);
        }

        if (Build.VERSION.SDK_INT >= 23) {
            mTimePicker.setHour(mHour);
            mTimePicker.setMinute(mMinute);
        }
        else {
            mTimePicker.setCurrentHour(mHour);
            mTimePicker.setCurrentMinute(mMinute);
        }
        mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                mHour = hourOfDay;
                mMinute = minute;
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendResult(Activity.RESULT_OK);
                    }
                })
                .create();
    }

    public void sendResult(int resultCode) {
        if(getTargetFragment() == null) {
            return;
        }
        Intent i = new Intent();
        i.putExtra(EXTRA_HOUR, mHour);
        i.putExtra(EXTRA_MINUTE, mMinute);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
    }

    public static TimePickerFragment newInstance(int hour, int minute) {
        TimePickerFragment fragment = new TimePickerFragment();
        Bundle args = new Bundle();
        args.putInt(EXTRA_HOUR, hour);
        args.putInt(EXTRA_MINUTE, minute);
        fragment.setArguments(args);
        return fragment;
    }
}
