package com.pressco.s24bbkeyboard;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.Typeface;
import android.inputmethodservice.InputMethodService;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BBKeyboardService extends InputMethodService {
    private static final int BG=Color.rgb(9,11,14), KEY=Color.rgb(38,43,50), ACCENT=Color.rgb(0,199,183);
    private FrameLayout stage; private boolean caps=false, symbols=false; private final StringBuilder word=new StringBuilder();
    @Override public View onCreateInputView(){ return buildKeyboard(); }
    private View buildKeyboard(){
        stage=new FrameLayout(this); stage.setBackgroundColor(BG);
        LinearLayout panel=new LinearLayout(this); panel.setOrientation(LinearLayout.VERTICAL); panel.setPadding(dp(3),dp(4),dp(3),dp(7));
        TextView privacy=new TextView(this); privacy.setText("OFFLINE  •  S24 BB  •  FLYING WORDS"); privacy.setTextColor(ACCENT); privacy.setTextSize(11); privacy.setGravity(Gravity.CENTER); panel.addView(privacy,new LinearLayout.LayoutParams(-1,dp(28)));
        addRow(panel,"QWERTYUIOP"); addRow(panel,"ASDFGHJKL"); addRow(panel,"ZXCVBNM"); addControls(panel);
        stage.addView(panel,new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM)); return stage;
    }
    private void addRow(LinearLayout panel,String letters){
        LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER);
        for(char c:letters.toCharArray()){ TextView k=key(String.valueOf(c),1f); k.setOnClickListener(v->type(((TextView)v).getText().toString())); row.addView(k); }
        panel.addView(row,new LinearLayout.LayoutParams(-1,dp(54)));
    }
    private void addControls(LinearLayout panel){
        LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER);
        TextView shift=key("⇧",1.1f); shift.setOnClickListener(v->{caps=!caps; rebuild();}); row.addView(shift);
        TextView sym=key(symbols?"ABC":"?123",1.2f); sym.setOnClickListener(v->{symbols=!symbols; rebuild();}); row.addView(sym);
        TextView comma=key(",",0.8f); comma.setOnClickListener(v->type(",")); row.addView(comma);
        TextView space=key("SPACE",3.2f); space.setOnClickListener(v->space()); row.addView(space);
        TextView period=key(".",0.8f); period.setOnClickListener(v->type(".")); row.addView(period);
        TextView back=key("⌫",1.1f); back.setOnClickListener(v->backspace()); back.setOnLongClickListener(v->{backspace(); return true;}); row.addView(back);
        TextView enter=key("↵",1.1f); enter.setOnClickListener(v->sendKey(KeyEvent.KEYCODE_ENTER)); row.addView(enter);
        panel.addView(row,new LinearLayout.LayoutParams(-1,dp(58)));
    }
    private TextView key(String label,float weight){
        TextView v=new TextView(this); v.setText(label); v.setTextColor(Color.WHITE); v.setTextSize(label.length()>2?13:20); v.setTypeface(Typeface.DEFAULT,Typeface.BOLD); v.setGravity(Gravity.CENTER); v.setBackgroundColor(KEY); v.setPadding(dp(1),0,dp(1),0);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,weight); lp.setMargins(dp(2),dp(3),dp(2),dp(3)); v.setLayoutParams(lp); v.setOnTouchListener((x,e)->{if(e.getAction()==0)x.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); return false;}); return v;
    }
    private void type(String raw){ String s=caps?raw.toUpperCase():raw.toLowerCase(); if(symbols){String map="1234567890"; int i="QWERTYUIOP".indexOf(raw.toUpperCase()); if(i>=0)s=String.valueOf(map.charAt(i));}
        InputConnection ic=getCurrentInputConnection(); if(ic!=null)ic.commitText(s,1); if(Character.isLetterOrDigit(s.charAt(0)))word.append(s); if(caps){caps=false;}
    }
    private void space(){ String done=word.toString(); InputConnection ic=getCurrentInputConnection(); if(ic!=null)ic.commitText(" ",1); if(!done.isEmpty())fly(done); word.setLength(0); }
    private void backspace(){ InputConnection ic=getCurrentInputConnection(); if(ic!=null)ic.deleteSurroundingText(1,0); if(word.length()>0)word.deleteCharAt(word.length()-1); }
    private void sendKey(int code){ InputConnection ic=getCurrentInputConnection(); if(ic!=null){ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,code));ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,code));} word.setLength(0); }
    private void fly(String text){
        TextView f=new TextView(this); f.setText(text); f.setTextColor(ACCENT); f.setTextSize(24); f.setTypeface(Typeface.DEFAULT,Typeface.BOLD); f.setGravity(Gravity.CENTER); f.setShadowLayer(10,0,0,ACCENT);
        FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-1,dp(48),Gravity.BOTTOM); lp.bottomMargin=dp(32); stage.addView(f,lp);
        ObjectAnimator up=ObjectAnimator.ofFloat(f,"translationY",0,-dp(185)); ObjectAnimator fade=ObjectAnimator.ofFloat(f,"alpha",1f,0f); ObjectAnimator scaleX=ObjectAnimator.ofFloat(f,"scaleX",0.7f,1.12f); ObjectAnimator scaleY=ObjectAnimator.ofFloat(f,"scaleY",0.7f,1.12f);
        AnimatorSet a=new AnimatorSet(); a.playTogether(up,fade,scaleX,scaleY); a.setDuration(720); a.addListener(new android.animation.AnimatorListenerAdapter(){@Override public void onAnimationEnd(android.animation.Animator x){stage.removeView(f);}}); a.start();
    }
    private void rebuild(){ setInputView(buildKeyboard()); }
    private int dp(int n){ return Math.round(n*getResources().getDisplayMetrics().density); }
}
