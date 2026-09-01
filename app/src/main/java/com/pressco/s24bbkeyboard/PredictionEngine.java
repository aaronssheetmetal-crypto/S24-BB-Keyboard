package com.pressco.s24bbkeyboard;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PredictionEngine {
    private final SharedPreferences prefs;private final Map<String,Integer> frequency=new HashMap<>();
    private static final String[] WORDS=("about after again all also always and another any apple are around as ask back because been before being best better between both build business but by can change check come company could customer day did different do does done down each end even every example factory find first flick for from get give go good great had has have he help here high how if important in into is it its job just keep keyboard know last like little look make many may me more most much must my need new next no not now of off on one only or other our out over people please price project put right said same see she should show so some start still take than that the their them then there these they thing think this time to today too two up use very want was way we well were what when where which who why will with word work would yes you your aluminium automotive bracket build buyer casting chennai cnc component cost drawing engineering estimate fabrication finish machine machining material metal production quality quote sales schedule sheet stainless steel supplier tamil nadu welding").split(" ");
    public PredictionEngine(Context c){prefs=c.getSharedPreferences("local_dictionary",Context.MODE_PRIVATE);for(String w:WORDS)frequency.put(w,10);for(String key:prefs.getAll().keySet())frequency.put(key,prefs.getInt(key,1)+20);}
    public List<String> predict(String prefix,int limit){String p=prefix.toLowerCase(Locale.ROOT);List<String> out=new ArrayList<>();for(String w:frequency.keySet())if(w.startsWith(p)&&w.length()>p.length())out.add(w);Collections.sort(out,(a,b)->{int f=Integer.compare(frequency.getOrDefault(b,0),frequency.getOrDefault(a,0));return f!=0?f:Integer.compare(a.length(),b.length());});return out.subList(0,Math.min(limit,out.size()));}
    public void learn(String raw){String w=raw.toLowerCase(Locale.ROOT).replaceAll("[^a-z']","");if(w.length()<2)return;int n=frequency.getOrDefault(w,0)+1;frequency.put(w,n);prefs.edit().putInt(w,Math.min(n,1000)).apply();}
}
