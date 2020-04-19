package org.me.gcu.trafficapplication;

import android.app.ProgressDialog;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

public class CustomProgressDialog extends AppCompatActivity {

    private ProgressDialog progressDialog;

    CustomProgressDialog(Context context) {
        progressDialog = new ProgressDialog(context);
    }

    public void displayLoadingDialog() {
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    public void removeLoadingDialog() {
        progressDialog.dismiss();
    }

}
