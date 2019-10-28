package com.tigcal.helpme;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AboutDialog extends DialogFragment {
    private static final String DIALOG_TAG = AboutDialog.class.getSimpleName();

    public AboutDialog() {
        //Empty Constructor
    }

    public static AboutDialog newInstance() {
        return new AboutDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_about, container);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = getContext();
        getDialog().setTitle(context.getString(R.string.about_header));

        final StringBuilder appVersionBuilder = new StringBuilder();
        appVersionBuilder.append("\n");
        appVersionBuilder.append(context.getString(R.string.app_name));
        try {
            appVersionBuilder.append(" ")
                    .append(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(DIALOG_TAG, "package name not found");
        }
        appVersionBuilder.append("\n");

        final TextView appVersion = (TextView) view.findViewById(R.id.app_version);
        appVersion.setText(appVersionBuilder.toString());

        MovementMethod linkMovementMethod = LinkMovementMethod.getInstance();
        final TextView appDevelopers = (TextView) view.findViewById(R.id.about_developer);
        appDevelopers.setMovementMethod(linkMovementMethod);
        final TextView contactTextView = (TextView) view.findViewById(R.id.about_contact_info);
        contactTextView.setMovementMethod(linkMovementMethod);
    }

}
