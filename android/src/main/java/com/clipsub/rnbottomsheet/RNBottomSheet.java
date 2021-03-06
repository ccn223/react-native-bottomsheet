package com.clipsub.rnbottomsheet;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.telecom.Call;

import com.cocosw.bottomsheet.BottomSheet;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RNBottomSheet extends ReactContextBaseJavaModule {

    private boolean isOpened;
    private Callback shareSuccessCallback;
    private Callback shareFailureCallback;

    public RNBottomSheet(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "RNBottomSheet";
    }

    @ReactMethod
    public void showBottomSheetWithOptions(ReadableMap options, final Callback onSelect) {
        if (this.isOpened) return;

        this.isOpened = true;

        ReadableArray optionArray = options.getArray("options");
        final Integer cancelButtonIndex = options.getInt("cancelButtonIndex");
        String title;
        boolean dark = false;
        BottomSheet.Builder builder;

        // Title.
        try {
            title = options.getString("title");
            builder = new BottomSheet.Builder(this.getCurrentActivity()).title(title);
        } catch (Exception e) {
            builder = new BottomSheet.Builder(this.getCurrentActivity());
        }

        // Dark theme.
        try {
          dark = options.getBoolean("dark");
          if (dark) {
            builder.darkTheme();
          }
        } catch (Exception e) {
          // Code...
        }

        // Options.
        Integer size = optionArray.size();
        for (int i = 0; i < size; i++) {
            builder.sheet(i, optionArray.getString(i));
        }

        builder.listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                // if (which != cancelButtonIndex) {
                    // invoke callback when cancel bottom sheet
                    onSelect.invoke(which);
                // }
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                RNBottomSheet.this.isOpened = false;
            }
        });

        builder.build().show();
    }

    @ReactMethod
    public void showShareBottomSheetWithOptions(ReadableMap options, Callback failureCallback, Callback successCallback) {
        String url = options.getString("url");
        String message = options.getString("message");
        String subject = options.getString("subject");

        List<String> items = new ArrayList<>();
        if (message != null && !message.isEmpty()) {
            items.add(message);
        }

        final Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        Uri uri = Uri.parse(url);
        if (uri != null) {
            if (uri.getScheme() != null && "data".equals(uri.getScheme().toLowerCase())) {
                shareIntent.setType("*/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            } else {
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_EMAIL, url);
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                shareIntent.putExtra(Intent.EXTRA_TEXT, message);
            }
        }

        this.shareSuccessCallback = successCallback;
        this.shareFailureCallback = failureCallback;

        if (shareIntent.resolveActivity(this.getCurrentActivity().getPackageManager()) != null) {
            this.getCurrentActivity().startActivity(Intent.createChooser(shareIntent, "Share To"));
        } else {
            failureCallback.invoke(new Exception("The app you want to share is not installed."));
        }
    }

}
