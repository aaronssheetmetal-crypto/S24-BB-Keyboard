package com.pressco.s24bbkeyboard;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.Typeface;
import android.inputmethodservice.InputMethodService;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BBKeyboardService extends InputMethodService {
    private static final int BG=Color.rgb(9,11,14), KEY=Color.rgb(38,43,50), ACCENT=Color.rgb(0,220,195);
    private FrameLayout stage; private LinearLayout panel; private boolean caps=false, symbols=false;
    private final StringBuilder prefix=new StringBuilder();
    private final Map<Character,TextView> labels=new HashMap<>();
    private final Map<Character,String> predictions=new HashMap<>();
    private PredictionEngine engine;

    @Override public void onCreate(){super.onCreate();engine=new PredictionEngine(this);}
    @Override public View onCreateInputView(){return buildKeyboard();}
    @Override public void onStartInputView(android.view.inputmethod.EditorInfo info,boolean restarting){super.onStartInputView(info,restarting);prefix.setLength(0);updatePredictions();}

    private View buildKeyboard(){
        stage=new FrameLayout(this);stage.setBackgroundColor(BG);labels.clear();predictions.clear();
        panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(dp(3),dp(3),dp(3),dp(7));
        TextView banner=new TextView(this);banner.setText("FLICK UP A WORD  •  OFFLINE");banner.setTextColor(ACCENT);banner.setTextSize(11);banner.setGravity(Gravity.CENTER);panel.addView(banner,new LinearLayout.LayoutParams(-1,dp(26)));
        addRow("QWERTYUIOP");addRow("ASDFGHJKL");addRow("ZXCVBNM");addControls();
        stage.addView(panel,new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM));updatePredictions();return stage;
    }

    private void addRow(String letters){LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);for(char c:letters.toCharArray())row.addView(letterKey(c),new LinearLayout.LayoutParams(0,dp(68),1f));panel.addView(row,new LinearLayout.LayoutParams(-1,dp(68)));}

    private View letterKey(char letter){
        FrameLayout box=new FrameLayout(this);FrameLayout.LayoutParams outer=new FrameLayout.LayoutParams(-1,-1);outer.setMargins(dp(2),dp(2),dp(2),dp(2));box.setLayoutParams(outer);box.setBackgroundColor(KEY);
        TextView prediction=new TextView(this);prediction.setTextColor(ACCENT);prediction.setTextSize(10);prediction.setTypeface(Typeface.DEFAULT,Typeface.BOLD);prediction.setGravity(Gravity.CENTER);prediction.setMaxLines(1);box.addView(prediction,new FrameLayout.LayoutParams(-1,dp(27),Gravity.TOP));labels.put(letter,prediction);
        TextView key=new TextView(this);key.setText(String.valueOf(letter));key.setTextColor(Color.WHITE);key.setTextSize(20);key.setTypeface(Typeface.DEFAULT,Typeface.BOLD);key.setGravity(Gravity.CENTER);box.addView(key,new FrameLayout.LayoutParams(-1,dp(42),Gravity.BOTTOM));
        box.setOnTouchListener(new View.OnTouchListener(){float downY;
            @Override public boolean onTouch(View v,MotionEvent e){if(e.getAction()==MotionEvent.ACTION_DOWN){downY=e.getY();v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);return true;}if(e.getAction()==MotionEvent.ACTION_UP){String predicted=predictions.get(letter);if(e.getY()-downY < -dp(22)&&predicted!=null){acceptPrediction(predicted);return true;}typeLetter(letter);return true;}return true;}});return box;
    }

    private void addControls(){
        LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);
        row.addView(control("⇧",1.0f,v->{caps=!caps;rebuild();}));row.addView(control(symbols?"ABC":"?123",1.2f,v->{symbols=!symbols;rebuild();}));row.addView(control(",",0.75f,v->punctuate(",")));row.addView(control("SPACE",3.1f,v->space()));row.addView(control(".",0.75f,v->punctuate(".")));row.addView(control("⌫",1.0f,v->backspace()));row.addView(control("↵",1.0f,v->sendKey(KeyEvent.KEYCODE_ENTER)));panel.addView(row,new LinearLayout.LayoutParams(-1,dp(57)));
    }

    private View control(String label,float weight,View.OnClickListener click){TextView v=new TextView(this);v.setText(label);v.setTextColor(Color.WHITE);v.setTextSize(label.length()>2?12:19);v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);v.setGravity(Gravity.CENTER);v.setBackgroundColor(KEY);v.setOnClickListener(click);v.setOnTouchListener((x,e)->{if(e.getAction()==MotionEvent.ACTION_DOWN)x.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);return false;});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,weight);lp.setMargins(dp(2),dp(3),dp(2),dp(3));v.setLayoutParams(lp);return v;}

    private void typeLetter(char raw){
        String s=String.valueOf(caps?Character.toUpperCase(raw):Character.toLowerCase(raw));
        if(symbols){String keys="QWERTYUIOPASDFGHJKLZXCVBNM",vals="1234567890@#$%&*-+()!?/:;";int i=keys.indexOf(raw);if(i>=0&&i<vals.length())s=String.valueOf(vals.charAt(i));}
        InputConnection ic=getCurrentInputConnection();if(ic!=null)ic.commitText(s,1);if(Character.isLetter(s.charAt(0)))prefix.append(s.toLowerCase(Locale.ROOT));else prefix.setLength(0);if(caps)caps=false;updatePredictions();
    }

    private void updatePredictions(){
        if(labels.isEmpty()||engine==null)return;for(TextView t:labels.values())t.setText("");predictions.clear();String p=prefix.toString();if(p.isEmpty()||symbols)return;List<String> words=engine.predict(p,8);
        for(String w:words){if(w.length()<=p.length())continue;char next=Character.toUpperCase(w.charAt(p.length()));if(!predictions.containsKey(next)&&labels.containsKey(next)){predictions.put(next,w);labels.get(next).setText(w);}}
    }

    private void acceptPrediction(String word){InputConnection ic=getCurrentInputConnection();if(ic!=null){String rest=word.substring(Math.min(prefix.length(),word.length()));ic.commitText(rest+" ",1);}engine.learn(word);prefix.setLength(0);fly(word);updatePredictions();}
    private void space(){String completed=prefix.toString();InputConnection ic=getCurrentInputConnection();if(ic!=null)ic.commitText(" ",1);if(completed.length()>1)engine.learn(completed);prefix.setLength(0);updatePredictions();}
    private void punctuate(String s){InputConnection ic=getCurrentInputConnection();if(ic!=null)ic.commitText(s,1);prefix.setLength(0);updatePredictions();}
    private void backspace(){InputConnection ic=getCurrentInputConnection();if(ic!=null)ic.deleteSurroundingText(1,0);if(prefix.length()>0)prefix.deleteCharAt(prefix.length()-1);updatePredictions();}
    private void sendKey(int code){InputConnection ic=getCurrentInputConnection();if(ic!=null){ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,code));ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,code));}prefix.setLength(0);updatePredictions();}

    private void fly(String text){TextView f=new TextView(this);f.setText(text);f.setTextColor(ACCENT);f.setTextSize(25);f.setTypeface(Typeface.DEFAULT,Typeface.BOLD);f.setGravity(Gravity.CENTER);f.setShadowLayer(12,0,0,ACCENT);FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-1,dp(48),Gravity.BOTTOM);lp.bottomMargin=dp(70);stage.addView(f,lp);ObjectAnimator up=ObjectAnimator.ofFloat(f,"translationY",0,-dp(210));ObjectAnimator fade=ObjectAnimator.ofFloat(f,"alpha",1f,0f);ObjectAnimator sx=ObjectAnimator.ofFloat(f,"scaleX",0.65f,1.2f);ObjectAnimator sy=ObjectAnimator.ofFloat(f,"scaleY",0.65f,1.2f);AnimatorSet set=new AnimatorSet();set.playTogether(up,fade,sx,sy);set.setDuration(560);set.addListener(new android.animation.AnimatorListenerAdapter(){@Override public void onAnimationEnd(android.animation.Animator a){stage.removeView(f);}});set.start();}
    private void rebuild(){setInputView(buildKeyboard());}
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
}
