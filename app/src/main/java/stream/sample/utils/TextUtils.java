package stream.sample.utils;

import android.text.Html;
import android.text.Spanned;

import java.util.regex.Pattern;

public class TextUtils {

    public TextUtils()
    {

    }

    public static String ParseAddress(String address)
    {
        if (address != null)
        {
            if (address.matches(Pattern.quote("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}")))
            {
                //MMS sent from email address. Save email as address
                return address;
            }
            else
            {
                address = address.replaceAll("^\\+1", "") //remove area code
                        .replaceAll("[-]", "")
                        .replaceAll("\\s+", "")
                        .replaceAll("\\(", "")
                        .replaceAll("\\)", "");
                if (address.length() == 11)
                {
                    if(address.substring(0,1).equals("1"))
                    {
                        address = address.substring(1);
                    }
                }
//                        .replaceAll("[^0-9]", ""); //remove all the non numbers
            }
        }
        else
        {
        }
        return address;
    }


    public static String CleanShare(String raw)
    {
        String output = "";
        output = raw.replaceAll("<br>", "\n");
        return output;
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html)
    {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }

    public static boolean isValidEmail(String email) {

        if (android.text.TextUtils.isEmpty(email))
        {
            return false;
        }
        else
        {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }
    }

    public static boolean isValidPassword(String password)
    {
        if (android.text.TextUtils.isEmpty(password))
        {
            return false;
        }
        else
        {
            return Pattern.compile("^[a-zA-Z0-9!@#$%^&*?]+$").matcher(password).matches();
        }
    }
}
