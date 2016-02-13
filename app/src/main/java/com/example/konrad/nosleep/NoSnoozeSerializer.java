package com.example.konrad.nosleep;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Created by Konrad on 1/31/2016.
 */
public class NoSnoozeSerializer {

    private Context mContext;
    private String mFileName;
    private static final String JSON_IP = "ip";
    private static final String TAG = "NoSnoozeSerializer: ";

    public NoSnoozeSerializer(Context c, String fileName) {
        mContext = c;
        mFileName = fileName;
    }

    public void saveIP(String IPAddress) throws JSONException, IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_IP, IPAddress);
        Writer writer = null;
        try {
            OutputStream out = mContext.openFileOutput(mFileName, Context.MODE_PRIVATE);
            writer = new OutputStreamWriter(out);
            writer.write(jsonObject.toString());
        }
        finally {
            if (writer != null) {
                writer.close();
            }
        }
        Log.i(TAG, "Saving successful");
    }

    public String loadIP() throws IOException, JSONException {
        String IPString = null;
        BufferedReader reader = null;
        try {
            InputStream in = mContext.openFileInput(mFileName);
            InputStreamReader innerReader = new InputStreamReader(in);
            reader = new BufferedReader(innerReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            JSONTokener tokener = new JSONTokener(stringBuilder.toString());
            JSONObject jsonObject = (JSONObject)tokener.nextValue();
            IPString = jsonObject.getString(JSON_IP);
            Log.i(TAG, "Loading successful");
        } catch (FileNotFoundException e) {

        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return IPString;
    }


}
