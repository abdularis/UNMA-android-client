package com.paperplanes.unma.common;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.paperplanes.unma.R;

import java.util.List;

/**
 * Created by abdularis on 27/11/17.
 */

public final class AppUtil {

    public static final int REQUEST_WRITE_PERM_CODE = 1;

    public static boolean isAppInForeground(Context context, String appPackageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            List<ActivityManager.RunningAppProcessInfo> processes = activityManager.getRunningAppProcesses();
            if (processes != null) {
                for (ActivityManager.RunningAppProcessInfo processInfo : processes) {
                    if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                            processInfo.processName.equalsIgnoreCase(appPackageName)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean checkWritePermission(Context context) {
        int result = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestWritePermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERM_CODE);
    }

    public static void requestWritePermission(Fragment fragment) {
        fragment.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERM_CODE);
    }

    public static void checkGooglePlayServicesAvailability(Context activityContext) {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int resultCode = api.isGooglePlayServicesAvailable(activityContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (api.isUserResolvableError(resultCode)) {
                api.getErrorDialog((Activity) activityContext, resultCode, 1234).show();
            }
            else {
                Toast.makeText(activityContext, R.string.text_unsupported_device, Toast.LENGTH_LONG).show();
                ((Activity) activityContext).finish();
            }
        }
    }
}
