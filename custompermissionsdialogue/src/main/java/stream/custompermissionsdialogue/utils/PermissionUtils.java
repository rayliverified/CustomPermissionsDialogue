package stream.custompermissionsdialogue.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Telephony;

public class PermissionUtils {

    public static boolean IsPermissionEnabled(Context context, String permission)
    {
        if (Build.VERSION.SDK_INT >= 23) {
            return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        else
        {
            return true;
        }
    }

    public static boolean IsPermissionsEnabled(Context context, String[] permissionList)
    {
        for (String permission : permissionList)
        {
            if (!IsPermissionEnabled(context, permission))
            {
                return false;
            }
        }

        return true;
    }

    public static boolean IsDefaultSMS(Context context)
    {
        if (Build.VERSION.SDK_INT >= 19) {
            String defaultSMSApp = Telephony.Sms.getDefaultSmsPackage(context);
            if (!defaultSMSApp.equals(context.getPackageName()))
            {
                return false;
            }
        }
        return true;
    }
}
