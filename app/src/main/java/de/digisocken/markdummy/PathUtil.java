package de.digisocken.markdummy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;

/**
 * Created by Aki on 1/7/2017.
 *
 * based on
 * https://stackoverflow.com/questions/13209494/how-to-get-the-full-file-path-from-uri
 *
 * but changed, because reading from /Documents/ hide the "Documents" path :-S
 */

public class PathUtil {
    /*
     * Gets the file path of the given Uri.
     */
    @SuppressLint("NewApi")
    public static String getPath(Context context, Uri uri) {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;

        if (needToCheckUri && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");

                Log.d(MarkdownActivity.PACKAGE_NAME, "uri get path from: " + uri.toString());
                if (uri.toString().startsWith("content://com.android.externalstorage.documents/document/home%3A")) {
                    return Environment.getExternalStorageDirectory() + "/Documents/" + split[1];
                } else {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(uri)) {
                Log.w(MarkdownActivity.PACKAGE_NAME, "files from magic downloads folder not supported");

            } else if (isMediaDocument(uri)) {
                Log.w(MarkdownActivity.PACKAGE_NAME, "files from magic media folder not supported");

            }
        }

        return uri.getPath();
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
}
