package com.michiganhackers.diabeticons.Util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.michiganhackers.diabeticons.Icon;
import com.michiganhackers.diabeticons.R;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by jawad on 12/10/15.
 */
public class Util {
    public static String KEY_INDEX = "IndexKey";
    public static String KEY_PATH = "PathKey";
    public static String KEY_TITLE = "TitleKey";

    // Easily encodes a url
    public static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            Log.wtf("MD/Util", "UTF-8 should always be supported", e);
            throw new RuntimeException("URLEncoder.encode() failed for " + s);
        }
    }

    public static void sendImage(Context context, Icon icon) {
        // Create the uri path to the image itself [thank you AssetsProvider Utils class]
        Uri imageUri = Uri.parse("content://com.michiganhackers.diabeticons/" + icon.getPath());

        // Create a general implicit intent with just the image
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);

        // Send the intent finally (and always use the chooser dialogue)
        context.startActivity(
                Intent.createChooser(shareIntent, context.getResources().getText(R.string.share_chooser_title))
        );
    }
}
