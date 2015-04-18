package com.tigcal.helpme;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.widget.TextView;

public class AboutDialog extends Dialog {

    public AboutDialog(Context context) {
        super(context);
        setTitle("About");
        setContentView(R.layout.dialog_about);
        setCancelable(true);

        final StringBuilder appVersionBuilder = new StringBuilder();
        appVersionBuilder.append("\n");
        appVersionBuilder.append(context.getString(R.string.app_name));
        try {
            appVersionBuilder.append(" " + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            appVersionBuilder.append("");
        }
        appVersionBuilder.append("\n");

        final TextView appVersion = (TextView) findViewById(R.id.app_version);
        appVersion.setText(appVersionBuilder.toString());

        MovementMethod linkMovementMethod = LinkMovementMethod.getInstance();
        final TextView appDevelopers = (TextView) findViewById(R.id.about_developer);
        appDevelopers.setMovementMethod(linkMovementMethod);
        final TextView contactTextView = (TextView) findViewById(R.id.about_contact_info);
        contactTextView.setMovementMethod(linkMovementMethod);

    }

}
