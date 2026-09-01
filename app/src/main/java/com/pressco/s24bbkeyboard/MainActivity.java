package com.pressco.s24bbkeyboard;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setGravity(Gravity.CENTER); root.setPadding(48,48,48,48); root.setBackgroundColor(Color.rgb(17,21,26));
        TextView title = new TextView(this); title.setText("S24 BB Keyboard"); title.setTextSize(28); title.setTextColor(Color.WHITE); title.setGravity(Gravity.CENTER); root.addView(title,new LinearLayout.LayoutParams(-1,-2));
        TextView note = new TextView(this); note.setText("Offline BlackBerry-style keyboard with flying-word animation.\n\n1. Enable the keyboard\n2. Select S24 BB Keyboard\n3. Open any app and type"); note.setTextSize(17); note.setTextColor(Color.LTGRAY); note.setGravity(Gravity.CENTER); note.setPadding(0,32,0,32); root.addView(note,new LinearLayout.LayoutParams(-1,-2));
        Button enable = button("Enable keyboard"); enable.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))); root.addView(enable,new LinearLayout.LayoutParams(-1,-2));
        Button select = button("Select keyboard"); select.setOnClickListener(v -> { android.view.inputmethod.InputMethodManager imm=(android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE); imm.showInputMethodPicker(); }); root.addView(select,new LinearLayout.LayoutParams(-1,-2));
        setContentView(root);
    }
    private Button button(String text){ Button b=new Button(this); b.setText(text); b.setTextSize(17); b.setAllCaps(false); return b; }
}
