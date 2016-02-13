package com.example.konrad.nosleep;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * A simple {@link Fragment} subclass.
 */
public class NoSnoozeFragment extends Fragment {
    RelativeLayout mMainLayout;
    EditText mIPEditText;
    ImageView mConnectionButton;
    TextView mTimeTextView;
    TextView mSongTextView;
    TextView mArtistTextView;
    ImageView mPreviousButton;
    ImageView mPauseButton;
    ImageView mPlayButton;
    ImageView mNextButton;

    String mIPAddress = "192.168.1.9";
    String mPort = "8001";
    boolean mIPAddressChanged = false;
    private static final String FILENAME = "connectionInfo.json";
    NoSnoozeSerializer mSerializer;
    private static final String TAG = "NoSnoozeFragment: ";

    boolean mConnected = false;

    int mHour = 6;
    int mMinute = 0;

    private JSONArray mMP3LibraryArray;
    private int mMP3Index = 0;
    private int mAlarmSongIndex = -1;

    public static final int REQUEST_TIME = 0;
    private static final String DIALOG_TIME = "time";

    private Socket mSocket;



    public NoSnoozeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_main, null);
        mMainLayout = (RelativeLayout)v.findViewById(R.id.mainLayout);
        mIPEditText = (EditText)v.findViewById(R.id.ipEditText);
        // CHECKING TO SEE IF WE HAVE A SAVED IP ADDRESS FROM BEFORE
        mSerializer = new NoSnoozeSerializer(getActivity().getApplicationContext(), FILENAME);
        String savedIPAddress = null;
        try {
            savedIPAddress = mSerializer.loadIP();
        } catch (Exception e) {
            Log.i(TAG, "Error loading saved IP Address");
            e.printStackTrace();
        }
        if (savedIPAddress == null) {
            // Do not update our IPAddress since we don't have a saved IP Address to load in.
        }
        else {
            mIPAddress = savedIPAddress;
        }
        mIPEditText.setText(mIPAddress);
        mIPEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                mIPAddress = s.toString();
                mIPAddressChanged = true;
                Log.i(TAG, "IP ADDRESS CHANGED");
            }
        });
        mTimeTextView = (TextView)v.findViewById(R.id.timeTextView);
        mTimeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment dialog = TimePickerFragment.newInstance(mHour, mMinute);
                dialog.setTargetFragment(NoSnoozeFragment.this, REQUEST_TIME);
                FragmentManager fm = getFragmentManager();
                dialog.show(fm, DIALOG_TIME);
            }
        });
        mSongTextView = (TextView)v.findViewById(R.id.songTextView);
        mSongTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "ONCLICK");
                mAlarmSongIndex = mMP3Index;
                mSocket.emit("client_alarmSongIndex_set", Integer.toString(mAlarmSongIndex));
                Log.i(TAG, "NEW ALARM SONG INDEX IS: " + mAlarmSongIndex);
                mSongTextView.setEnabled(false);
                mArtistTextView.setEnabled(false);
                if (Build.VERSION.SDK_INT < 23) {
                    mSongTextView.setTextColor(getResources().getColor(R.color.clickAccent));
                    mArtistTextView.setTextColor(getResources().getColor(R.color.clickAccent));
                }
                else {
                    mSongTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.clickAccent));
                    mArtistTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.clickAccent));
                }
            }
        });
        mArtistTextView = (TextView)v.findViewById(R.id.artistTextView);
        mArtistTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlarmSongIndex = mMP3Index;
                mSocket.emit("client_alarmSongIndex_set", Integer.toString(mAlarmSongIndex));
                Log.i(TAG, "NEW ALARM SONG INDEX IS: " + mAlarmSongIndex);
                mSongTextView.setEnabled(false);
                mArtistTextView.setEnabled(false);
                if (Build.VERSION.SDK_INT < 23) {
                    mSongTextView.setTextColor(getResources().getColor(R.color.clickAccent));
                    mArtistTextView.setTextColor(getResources().getColor(R.color.clickAccent));
                }
                else {
                    mSongTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.clickAccent));
                    mArtistTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.clickAccent));
                }
            }
        });
        mPreviousButton = (ImageView)v.findViewById(R.id.previousButton);
        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPreviousButton.setBackground(getResources().getDrawable(R.drawable.previousaccent1));
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPreviousButton.setBackground(getResources().getDrawable(R.drawable.previous1));
                        if(mMP3Index >= 0) {
                            mMP3Index--;
                            updateTrackBar(mMP3Index);
                        }
                    }
                }, 250);
            }
        });
        mPauseButton = (ImageView)v.findViewById(R.id.pauseButton);
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPauseButton.setBackground(getResources().getDrawable(R.drawable.pauseaccent1));
                mSocket.emit("client_pause_request");
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPauseButton.setBackground(getResources().getDrawable(R.drawable.pause2));
                    }
                }, 250);
            }
        });
        mPlayButton = (ImageView)v.findViewById(R.id.playButton);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPlayButton.setBackground(getResources().getDrawable(R.drawable.playaccent1));
                try {
                    String song = mMP3LibraryArray.getJSONObject(mMP3Index).getString("song");
                    String artist = mMP3LibraryArray.getJSONObject(mMP3Index).getString("artist");
                    mSocket.emit("client_play_request", song + "-" + artist);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPlayButton.setBackground(getResources().getDrawable(R.drawable.play2));
                    }
                }, 250);
            }
        });
        mNextButton = (ImageView)v.findViewById(R.id.nextButton);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNextButton.setBackground(getResources().getDrawable(R.drawable.nextaccent1));
                if (mMP3Index < mMP3LibraryArray.length() - 1) {
                    mMP3Index++;
                    updateTrackBar(mMP3Index);
                }
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mNextButton.setBackground(getResources().getDrawable(R.drawable.next1));
                    }
                }, 250);
            }
        });
        mConnectionButton = (ImageView)v.findViewById(R.id.connectionImageView);
        mConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMainLayout.requestFocus();
                //  IF CONNECTED AND PRESS AGAIN WE WANT TO DISCONNECT
                if (mConnected == true) {
                    executeDisconnectOperations();
                    mSocket.disconnect();
                    mConnected = false;
                    displayConnectionFail();
                } else {
                    attemptConnection();
                }
            }
        });
        mTimeTextView.setEnabled(false);
        toggleTrackBar(false);
        toggleMediaButtons(false);
        // TRY TO AUTO-CONNECT ON INITIAL LOADING OF VIEW
        attemptConnection();
        return v;
    }

    public void attemptConnection() {
        try {
            String URIString = "http://" + mIPAddress + ":" + mPort;
            mSocket = IO.socket(URIString);
            setSocketListeners();
            mSocket.connect();
            Handler connectionHandler = new Handler();
            connectionHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mConnected != true) {
                        Log.i(TAG, "Connection failed.");
                        displayConnectionFail();
                    }
                }
            }, 5000);
        } catch (URISyntaxException e) {
            Log.i(TAG, "Connection failed.");
            displayConnectionFail();
        }
    }

    public void displayConnectionFail() {
        mConnectionButton.setBackgroundResource(R.drawable.offline);
        // CHANGE ICON BACK TO CONNECT AFTER A SECOND
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                mConnectionButton.setBackgroundResource(R.drawable.connect);
            }
        }, 1000);
    }

    public void executeDisconnectOperations() {
        Log.i(TAG, "Disconnected.");
        mConnected = false;
        removeSocketListeners();
        displayConnectionFail();
        // RESET INDEX TO 0
        mMP3Index = 0;
        mAlarmSongIndex = -1;
        toggleMediaButtons(false);
        mTimeTextView.setText("13:37");
        mTimeTextView.setEnabled(false);
        mSongTextView.setText("n  o  S  π  o  o  z  e");
        mArtistTextView.setText("");
        toggleTrackBar(false);
    }

    ///////////////////////////////////
    // SERVER-->CLIENT COMMUNICATION //
    ///////////////////////////////////
    public void setSocketListeners() {
        mSocket.on("server_successful_connection", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mConnected = true;
                        Log.i(TAG, "Successful connection.");
                        mConnectionButton.setBackgroundResource(R.drawable.online);
                        // ALLOW USER TO INTERACT ON OTHER UI COMPONENTS
                        mTimeTextView.setEnabled(true);
                        toggleMediaButtons(true);
                        toggleTrackBar(true);
                    }
                });
            }
        });
        mSocket.on("server_client_disconnected", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        executeDisconnectOperations();
                    }
                });
            }
        });
        // The server will emit this in two scenarios.
        // 1. On initial client --> server connection the server already has an alarm time set.
        // 2. On successful client --> server emit "client_alarm_set_request" where server alarm time is updated.
        mSocket.on("server_alarm_successfully_set", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.i(TAG, "Server side | attempting to set alarm");
                int hour;
                int minute;
                try {
                    JSONObject alarmTime = new JSONObject((String) args[0]);
                    hour = alarmTime.getInt("hour");
                    minute = alarmTime.getInt("minute");
                    mHour = hour;
                    mMinute = minute;
                    final String timeString = timeStringFormatter(hour, minute);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTimeTextView.setText(timeString);
                        }
                    });
                    Log.i(TAG, "Server side |server_alarm_successfully_set|");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        mSocket.on("server_MP3Library_fetched", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    mMP3LibraryArray = new JSONArray((String) args[0]);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateTrackBar(mMP3Index);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        mSocket.on("server_alarmSongIndex_fetched", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                mAlarmSongIndex = Integer.parseInt((String) args[0]);
                Log.i(TAG, "ALARM SONG INDEX IS: " + mAlarmSongIndex);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateTrackBar(mMP3Index);
                    }
                });
            }
        });
    }

    public void removeSocketListeners() {
        mSocket.off("server_successful_connection");
        mSocket.off("server_client_disconnected");
        mSocket.off("server_alarm_successfully_set");
        mSocket.off("server_MP3Library_fetched");
    }

    public void updateTrackBar(int index) {
        try {
            JSONObject mp3JSONObject = mMP3LibraryArray.getJSONObject(index);
            String song = "♫ " + mp3JSONObject.getString("song") + " ♫";
            String artist = mp3JSONObject.getString("artist");
            mSongTextView.setText(song);
            mArtistTextView.setText(artist);
            if (index == mAlarmSongIndex) {
                if (Build.VERSION.SDK_INT < 23) {
                    mSongTextView.setTextColor(getResources().getColor(R.color.clickAccent));
                    mArtistTextView.setTextColor(getResources().getColor(R.color.clickAccent));
                } else {
                    mSongTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.clickAccent));
                    mArtistTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.clickAccent));
                }
                mSongTextView.setEnabled(false);
                mArtistTextView.setEnabled(false);
            }
            else { // Need to renable and change back to original color if previously viewed song was the alarmSong
                if (mSongTextView.isEnabled() == false && mArtistTextView.isEnabled() == false) {
                    mSongTextView.setEnabled(true);
                    mArtistTextView.setEnabled(true);
                    if (Build.VERSION.SDK_INT < 23) {
                        mSongTextView.setTextColor(getResources().getColor(R.color.holoBlueLightClone));
                        mArtistTextView.setTextColor(getResources().getColor(R.color.holoBlueLightClone));
                    } else {
                        mSongTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.holoBlueLightClone));
                        mArtistTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.holoBlueLightClone));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void toggleTrackBar(boolean boolValue) {
        mSongTextView.setEnabled(boolValue);
        mArtistTextView.setEnabled(boolValue);
    }

    public void toggleMediaButtons(boolean boolValue) {
        mPreviousButton.setEnabled(boolValue);
        mPauseButton.setEnabled(boolValue);
        mPlayButton.setEnabled(boolValue);
        mNextButton.setEnabled(boolValue);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent i) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        ///////////////////////////////////
        // CLIENT --> SERVER COMMUNICATION //
        ///////////////////////////////////
        // WE HAVE SET A REQUESTED ALARM TIME
        if (requestCode == REQUEST_TIME) {
            mHour = i.getIntExtra(TimePickerFragment.EXTRA_HOUR, 6);
            mMinute = i.getIntExtra(TimePickerFragment.EXTRA_MINUTE, 0);
            // MAKE THIS INTO JSON THEN --> JSONSTRING AND SEND TO SERVER
            JSONObject alarmTime = new JSONObject();
            try {
                alarmTime.put("minute", new Integer(mMinute));
                alarmTime.put("hour", new Integer(mHour));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mSocket.emit("client_alarm_set_request", alarmTime.toString());
            Log.i(TAG, "Client side |alarm_set_request|: " + alarmTime.toString());
        }
    }

    public String timeStringFormatter(int hour, int minute) {
        String hourString = Integer.toString(hour);
        String minuteString = Integer.toString(minute);
        if(hour < 10) {
            hourString = "0" + Integer.toString(hour);
        }
        if(minute < 10) {
            minuteString = "0" + Integer.toString(minute);
        }
        String returnString = hourString + ":" + minuteString;
        return returnString;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "ON PAUSE CALLED");
        if (mIPAddressChanged) {
            try {
                Log.i(TAG, "Attempt to save IP");
                mSerializer.saveIP(mIPAddress);
                // RESET VALUE
                mIPAddressChanged = false;
            } catch (Exception e) {
                Log.e(TAG, "Saving failed");
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "ON DESTROY CALLED");
        if (mIPAddressChanged) {
            try {
                Log.i(TAG, "Attempt to save IP");
                mSerializer.saveIP(mIPAddress);
            } catch (Exception e) {
                Log.e(TAG, "Saving failed");
                e.printStackTrace();
            }
        }
        if (mSocket != null) {
            mSocket.disconnect();
            removeSocketListeners();
        }
    }
}
