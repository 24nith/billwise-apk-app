package com.billwise.app;

import android.app.Activity;
import android.content.Context;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.PluginMethod;

@CapacitorPlugin(name = "NativePrint")
public class NativePrintPlugin extends Plugin {
    @PluginMethod
    public void print(PluginCall call) {
        String html = call.getString("html", "");
        Activity activity = getActivity();
        activity.runOnUiThread(() -> {
            WebView webView = new WebView(activity);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    try {
                        PrintManager printManager = (PrintManager) activity.getSystemService(Context.PRINT_SERVICE);
                        if (printManager == null) throw new IllegalStateException("Printing is unavailable");
                        PrintAttributes attributes = new PrintAttributes.Builder()
                            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                            .build();
                        printManager.print("Billwise Invoice", view.createPrintDocumentAdapter("Billwise Invoice"), attributes);
                        call.resolve();
                    } catch (Exception error) {
                        call.reject("Could not open the print service", error);
                    }
                }
            });
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
            call.resolve();
        });
    }
}
