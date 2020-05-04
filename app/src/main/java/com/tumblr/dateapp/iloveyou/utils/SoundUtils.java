package com.tumblr.dateapp.iloveyou.utils;

/**
 * Created by HS on 2018-01-24.
 */

import android.app.Activity;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SoundUtils {

    public static File decodeByteArray(String filename, byte[] bytes, Activity activity) throws FileNotFoundException {
        File tempAudioFile =
                null;
        try {
            //list of cached temp file

            //tempAudioFile = File.createTempFile(filename, "3gpp", activity.getCacheDir());
            File recordDir = getRecordDirectory(activity);
            tempAudioFile = new File(recordDir, filename);
            FileOutputStream fos = new FileOutputStream(tempAudioFile);
            fos.write(bytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempAudioFile;
    }

    @NonNull
    public static File getRecordDirectory(Activity activity) {
        File recordDir = new File(activity.getCacheDir(), "record");
        if (!recordDir.exists()) {
            recordDir.mkdir();
        }
        return recordDir;
    }

}
