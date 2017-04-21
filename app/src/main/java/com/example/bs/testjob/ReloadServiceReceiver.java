package com.example.bs.testjob;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class ReloadServiceReceiver extends BroadcastReceiver
{
    private String[] param = new String[7];
    private FileJob _Filesystem;//Вспомогательный класс для работы с файлами

    public ReloadServiceReceiver()
    {
        //
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        _Filesystem = new FileJob(context);//Вспомогательный класс для работы с файлами
        param =_Filesystem.ReadParams();
        if (param[0].isEmpty())
        {
            param = _Filesystem.GetDefaultParams();// настройки по умолчанию
        }

        Intent stIntent = new Intent(context, BatteryService.class);
        //Передаем в BatteryService параметры
        stIntent.putExtra("TextView_PARAM", param);

        context.startService(stIntent);
    }
}
