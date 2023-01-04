/*
* Автор сия безобразия - github.com/EugeneFitzrberg/SmsSender
*
* */

package com.example.sms_watcher;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    

    private final int CHECK_CODE = 0x1;
    private final int LONG_DURATION = 5000;
    private final int SHORT_DURATION = 1200;
    String[] newArr = new String[]{};
    private Speaker speaker;
    Dialog dialog;
    SQLiteDatabase db;
    private ToggleButton toggle;
    private CompoundButton.OnCheckedChangeListener toogleListener;
    private Button.OnClickListener messDetailListener;
    private ListView listView;
    private TextView smsText;
    private TextView smsSender;
    private Button closeMessDetailBtn;
    private ArrayAdapter<String> mAdapter;

    ArrayList<SMSInfo> smsList = new ArrayList<>();
    String[] arraySms = new String[]{};

    private BroadcastReceiver smsReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper();
        toggle = (ToggleButton) findViewById(R.id.speechToggle);
        smsText = (TextView) findViewById(R.id.sms_text);
        smsSender = (TextView) findViewById(R.id.sms_sender);
        listView = findViewById(R.id.listSMS);
        toogleListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                if (isChecked) {
                    speaker.allow(true);
                    speaker.speak(getString(R.string.start_speaking));
                } else {
                    speaker.speak(getString(R.string.stop_speaking));
                    speaker.allow(false);
                }
            }
        };

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String text = ((TextView) view).getText().toString();
                showMessDetails(text);
            }
        });

        toggle.setOnCheckedChangeListener(toogleListener);
        read();

//        delete();
//        dbHelper();

        checkTTS();
        initSMSReceiver();
        registerSMSReceiver();

        for (SMSInfo smsInfo : smsList) {
            newArr = addElement(newArr, smsInfo.text + " .Author: " + smsInfo.abonentName);
        }
        mAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, newArr);
        listView.setAdapter(mAdapter);
    }

    private void dbHelper() {
        db = getBaseContext().openOrCreateDatabase("mainInfo.db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS allData (messageText Text)");
    }

    public void delete() {
        db.execSQL("drop table  allData", null);
    }

    public void read() {
        Cursor query = db.rawQuery("Select * from allData", null);
        while (query.moveToNext()) {

            String[] splitText = query.getString(0).toString().split(" .Author: ");
            String number = "";
            String text = splitText[0];
            String abonentName = "";


            if (splitText.length > 1) {
                abonentName = splitText[1];
            }
            smsList.add(new SMSInfo(number, text, abonentName));
        }
        query.close();
    }

    @Override
    protected void onPause() {
        super.onPause();
        write();
    }

    public void write() {
        for (int i = 0; i < newArr.length; i++) {
            if (db.isOpen()) {
                db.execSQL("INSERT OR IGNORE INTO allData VALUES('" + newArr[i] + "')");
            }
        }

        Cursor query = db.rawQuery("Select * from allData", null);
        while (query.moveToNext()) {
            String dbString = query.getString(0);
            Log.i("DBC", dbString);
        }
        query.close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    void showMessDetails(String text) {
        MessageDetails md = new MessageDetails(this);
        md.show();
        String[] splitText = text.split(" .Author: ");
        String mesText = splitText[0];
        String abonent = splitText[1];
        md.abonentView.setText(abonent);
        md.messageView.setText(mesText);
        speaker.speak(mesText);
        md.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        md.closeMessDetailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                md.dismiss();
            }
        });
    }

    private void checkTTS() {
        Intent check = new Intent();
        check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(check, CHECK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                speaker = new Speaker(this);
            } else {
                Intent install = new Intent();
                install.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(install);
            }
        }
    }

    private void initSMSReceiver() {
        smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    for (int i = 0; i < pdus.length; i++) {
                        byte[] pdu = (byte[]) pdus[i];
                        SmsMessage message = SmsMessage.createFromPdu(pdu);
                        Log.i("TTT", "Bingo! Received SMS from: " + message.getOriginatingAddress());

                        String text = message.getDisplayMessageBody();
                        String sender = getContactName(message.getOriginatingAddress());
                        speaker.pause(LONG_DURATION);
                        speaker.speak("You have message from " + message.getOriginatingAddress());
                        speaker.pause(SHORT_DURATION);
                        speaker.speak("Message's text " + text);
                        smsSender.setText("Сообщение от " + sender);
                        smsText.setText(text);
                        smsList.add(new SMSInfo(message.getOriginatingAddress(), text, sender));
                        newArr = new String[]{};
                        for (SMSInfo smsInfo : smsList) {
                            newArr = addElement(newArr, smsInfo.text + " .Author: " + smsInfo.abonentName);
                        }

                        mAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, newArr);
                        listView.setAdapter(mAdapter);
                        write();
                    }
                }
            }
        };
    }

    private static String[] addElement(String[] myArray, String element) {
        String[] array = new String[myArray.length + 1];
        System.arraycopy(myArray, 0, array, 0, myArray.length);
        array[myArray.length] = element;
        return array;
    }

    private String getContactName(String phone) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        String projection[] = new String[]{ContactsContract.Data.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor.moveToFirst()) {
            return cursor.getString(0);
        } else {
            return "Неизвестный номер";
        }
    }

    private void registerSMSReceiver() {
        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, intentFilter);
    }
}