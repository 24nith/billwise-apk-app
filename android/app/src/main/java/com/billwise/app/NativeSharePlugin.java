package com.billwise.app;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.PluginMethod;

@CapacitorPlugin(name = "NativeShare")
public class NativeSharePlugin extends Plugin {
    @PluginMethod
    public void share(PluginCall call) {
        String title = call.getString("title", "Billwise Invoice");
        String text = call.getString("text", "");
        String packageName = call.getString("packageName", "");
        String imageData = call.getString("imageData", "");

        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TITLE, title);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        if (imageData != null && imageData.startsWith("data:image/")) {
            try {
                String[] parts = imageData.split(",", 2);
                String mimeType = parts[0].substring(5, parts[0].indexOf(';'));
                byte[] bytes = Base64.decode(parts[1], Base64.DEFAULT);
                String extension = mimeType.substring(mimeType.indexOf('/') + 1).toLowerCase(Locale.US);
                File imageFile = new File(getContext().getCacheDir(), "billwise-invoice." + extension);
                try (FileOutputStream output = new FileOutputStream(imageFile)) {
                    output.write(bytes);
                }
                Uri imageUri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", imageFile);
                sendIntent.setType(mimeType);
                sendIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                sendIntent.setClipData(ClipData.newRawUri("Invoice image", imageUri));
            } catch (Exception error) {
                call.reject("Could not prepare the invoice image", error);
                return;
            }
        } else {
            sendIntent.setType("text/plain");
        }
        if (!packageName.isEmpty()) sendIntent.setPackage(packageName);

        try {
            getActivity().startActivity(Intent.createChooser(sendIntent, title));
            call.resolve();
        } catch (Exception error) {
            call.reject("No compatible sharing app is installed", error);
        }
    }
}
