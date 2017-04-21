package com.example.bs.testjob;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

public class Activity_Mailsetting extends Activity
{
    private String[] MailParam;
    private Intent _MailIntent;
    public TextView mailAddress, mailServer, mailLogin, mailPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__mailsetting);

        _MailIntent = this.getIntent();
        MailParam = _MailIntent.getStringArrayExtra("TextView_PARAM");// Получили переданные параметры

        // Получаем ссылки на элементы
        mailAddress = (TextView)findViewById(R.id.mailAddress);
        mailServer = (TextView)findViewById(R.id.mailServer);
        mailLogin = (TextView)findViewById(R.id.mailLogin);
        mailPassword = (TextView)findViewById(R.id.mailPassword);

        // заполняем поля переданными параметрами
        mailAddress.setText(MailParam[0]);
        mailServer.setText(MailParam[1]+":"+MailParam[4]);
        mailLogin.setText(MailParam[2]);
        mailPassword.setText(MailParam[3]);
        /*
        FileJob _FileSupport = new FileJob(getApplicationContext());

        MultiAutoCompleteTextView mac = (MultiAutoCompleteTextView)findViewById(R.id.multiAutoCompleteTextView);
        String curstr = _FileSupport.ReadData();
        mac.setText(curstr);
        */
    }

    public void onClick_ButtonBack(View view)
    {
        Intent _intent = new Intent();
        String[] eparam = new String[5];
        eparam[0] = mailAddress.getText().toString();
        eparam[1] = mailServer.getText().toString().split(":")[0];
        eparam[2] = mailLogin.getText().toString();
        eparam[3] = mailPassword.getText().toString();
        eparam[4] = mailServer.getText().toString().split(":")[1];

        _intent.putExtra("MAIL_SETTING", eparam);
        setResult(RESULT_OK, _intent);
        finish();
    }
}
