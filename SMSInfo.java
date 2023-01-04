/*
 * Автор сия безобразия - github.com/EugeneFitzrberg/SmsSender
 *
 * */

package com.example.sms_watcher;

public class SMSInfo {
    String number;
    String text;
    String abonentName;

    SMSInfo(String number, String text, String abonentName){
        this.abonentName = abonentName;
        this.text = text;
        this.number = number;
    }

}
