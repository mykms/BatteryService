package com.example.bs.testjob;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class FileJob extends Activity
{
    private Context _context;
    private final String fnameSetting = "Android.BatterySetting", fnameData = "BatteryData.dat";
    private String MAIL = "";
    private String HOST = "";
    private String LOGIN = "";
    private String PASSWORD = "";
    private String PORT = "";

    private String TYPESTART = "";
    private String TIMES = "";

    public FileJob(Context _ContextMainApp)
    {
        this._context = _ContextMainApp;
        // Настройки по умолчанию
        MAIL = "test201701@yandex.ru";//e-mail
        HOST = "smtp.yandex.com";//smtp host
        LOGIN = "test201701";//login;
        PASSWORD = "123456789q";//password;
        PORT = "456";

        TYPESTART = "0";
        TIMES = "00:01:00";
    }
    public String[] GetDefaultParams()
    {
        String[] defParams = new String[7];
        // Настройки по умолчанию
        defParams[0] = "test201701@yandex.ru";//e-mail
        defParams[1] = "smtp.yandex.com";//smtp host
        defParams[2] = "test201701";//login;
        defParams[3] = "Password201701";//password;
        defParams[4] = "456";

        defParams[5] = "0";
        defParams[6] = "00:01:00";

        return defParams;
    }
    public String GetFileNameSetting()
    {
        return this.fnameSetting;
    }
    public String GetFileNameData()
    {
        return this.fnameData;
    }
    // TODO: Сохранение настроек в файл
    public void WriteParams(String[] InParam)
    {
        try
        {
            SharedPreferences _pref = _context.getSharedPreferences(fnameSetting, MODE_PRIVATE);
            SharedPreferences.Editor _editor = _pref.edit();
            _editor.putString("MAIL", InParam[0]);
            _editor.putString("HOST", InParam[1]);
            _editor.putString("LOGIN", InParam[2]);
            _editor.putString("PASSWORD", InParam[3]);
            _editor.putString("PORT", InParam[4]);
            _editor.putString("TYPESTART", InParam[5]);
            _editor.putString("TIMES", InParam[6]);

            _editor.commit();
        }
        catch (Exception ex)
        {
            ex.getMessage();
            ex.getStackTrace();
        }
    }
    // TODO: Загрузка настроек из файла.
    public String[] ReadParams()
    {
        String[] outParams = new String[7];
        try
        {
            SharedPreferences _pref = _context.getSharedPreferences(fnameSetting, MODE_PRIVATE);
            outParams[0] = _pref.getString("MAIL", "");
            outParams[1] = _pref.getString("HOST", "");
            outParams[2] = _pref.getString("LOGIN", "");
            outParams[3] = _pref.getString("PASSWORD", "");
            outParams[4] = _pref.getString("PORT", "");

            outParams[5] = _pref.getString("TYPESTART", "");
            outParams[6] = _pref.getString("TIMES", "");
        }
        catch (Exception ex)
        {
            outParams[0] = "";
            outParams[1] = "";
            outParams[2] = "";
            outParams[3] = "";
            outParams[4] = "";

            outParams[5] = "";
            outParams[6] = "";
        }
        return outParams;
    }

    public void WriteData(String datastring, boolean Append)
    {
        FileOutputStream fos = null;
        try
        {
            if (Append)
                fos = _context.openFileOutput(fnameData, _context.MODE_APPEND);
            else
                fos = _context.openFileOutput(fnameData, MODE_PRIVATE);
            fos.write(datastring.getBytes());
        }
        catch(IOException ex)
        {
            ex.getStackTrace();
        }
        finally
        {
            try
            {
                if(fos!=null)
                    fos.close();
            }
            catch(IOException ex)
            {
                ex.getStackTrace();
            }
        }
    }

    public String ReadData()
    {
        FileInputStream fin = null;
        StringBuilder buildStr = new StringBuilder();
        try {
            fin = _context.openFileInput(fnameData);
            InputStreamReader sr = new InputStreamReader(fin);
            BufferedReader br = new BufferedReader(sr);

            String temp_str = "";
            while ((temp_str = br.readLine()) != null )
            {
                String s = temp_str + "\n";
                buildStr.append(s);
            }
        }
        catch(IOException ex)
        {
            ex.getStackTrace();
        }
        finally
        {
            try{
                if(fin!=null)
                    fin.close();
            }
            catch(IOException ex)
            {
                ex.getStackTrace();
            }
        }
        return buildStr.toString();
    }
}
