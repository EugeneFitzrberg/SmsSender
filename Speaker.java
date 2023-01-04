/*
 * Автор сия безобразия - github.com/EugeneFitzrberg/SmsSender
 *
 * */

package com.example.sms_watcher;

import android.content.Context;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;

import java.util.HashMap;
import java.util.Locale;

public class Speaker implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private boolean ready = false;
    private boolean allowed = false;

    public Speaker(Context context){
        tts = new TextToSpeech(context,this);
    }

    public boolean isAllowed(){
        return allowed;
    }

    public void allow(boolean flag){
        this.allowed = flag;
    }

    @Override
    public void onInit(int i) {
        if(i == TextToSpeech.SUCCESS){
            tts.setLanguage(Locale.ENGLISH);
            ready = true;
        }else{
            ready = false;
        }
    }

    public void speak(String text){
        if(ready && allowed){
            HashMap<String,String> hashMap = new HashMap<String,String>();
            hashMap.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_NOTIFICATION));
            tts.speak(text,TextToSpeech.QUEUE_ADD,hashMap);
        }
    }

    public void pause(int duration){
        tts.playSilence(duration,TextToSpeech.QUEUE_ADD,null);
    }

    public void destroy(){
        tts.shutdown();
    }
}
