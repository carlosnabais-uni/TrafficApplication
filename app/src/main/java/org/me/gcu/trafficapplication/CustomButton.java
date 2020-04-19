package org.me.gcu.trafficapplication;

import android.content.Context;
import android.content.Intent;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

public class CustomButton extends AppCompatActivity {

    public Button createCustomButton(String text,
                                      final Context context,
                                      final Class customClass,
                                      final Map<String, String> intentValues,
                                      String drawableName)
    {
        Button button = new Button(
                new ContextThemeWrapper(
                        context,
                        drawableName == "success" ?
                                R.style.AppTheme_ButtonSuccess : R.style.AppTheme_Button));
        button.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        ));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, customClass);

                for(Map.Entry<String, String> entry: intentValues.entrySet()) {
                    intent.putExtra(entry.getKey(), entry.getValue());
                }
                context.startActivity(intent);
            }
        });
        button.setText(text);

        return button;
    }
}
