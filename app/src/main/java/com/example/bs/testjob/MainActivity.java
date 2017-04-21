package com.example.bs.testjob;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends Activity
{
    private ArrayAdapter<String> dataList;
    private ArrayList<String> _array;
    private String[] param;
    private ListView _list;
    private Button _AddItem, _ClearListButton;
    private short typeStart = 0;
    private FileJob _Filesystem;//Вспомогательный класс для работы с файлами

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        _Filesystem = new FileJob(getApplicationContext());//Вспомогательный класс для работы с файлами

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _list = (ListView)findViewById(R.id.ListViewItems);// Находим список

        String[] datastring = getResources().getStringArray(R.array.ListViewDefVal);
        _array = new ArrayList<String>();
        for (String it:datastring)
            _array.add(it);
        dataList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, _array);
        _list.setAdapter(dataList);

        _AddItem = (Button)findViewById(R.id.AddItem);
        _ClearListButton = (Button)findViewById(R.id.ClearListButton);
        _AddItem.setVisibility(View.INVISIBLE);// Кнопка невидна
        _ClearListButton.setVisibility(View.INVISIBLE);// Кнопка невидна
        _list.setVisibility(View.INVISIBLE);// и список тоже

        param = new String[7];
        String[] InParamsToLoad = null;

        try
        {
            InParamsToLoad = _Filesystem.ReadParams();
        }
        catch (Exception ex)
        {
            ex.getStackTrace();
        }
        finally
        {
            if (InParamsToLoad[0].isEmpty())
            {
                param = _Filesystem.GetDefaultParams();// настройки по умолчанию
            }
            else
            {
                param = InParamsToLoad;
            }
            typeStart = Short.valueOf(param[5]);
        }

        final Spinner _sp = (Spinner)findViewById(R.id.SpinnerId);// Получаем ссылку на раскрывающий список
        // Отображаем время в поле ввода или списке
        if (typeStart == 0)
        {
            EditText _inTime = (EditText) findViewById(R.id.editTextTime);
            _inTime.setText(param[6]);
        }
        if (typeStart == 1)
        {
            String[] _tmpTime = param[6].split(";");

            onClick_ClearListButton(null);
            for (int i = 0; i < _tmpTime.length; i++)
                dataList.insert(_tmpTime[i], i);
            dataList.notifyDataSetChanged();// Обновляем данные

            _sp.setSelection(1); // Указываем, что в списке выбрано значение №2
        }

        _sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                Object ob = _sp.getSelectedItem();
                TextView IntervalTextView = (TextView)findViewById(R.id.IntervalTextView);
                // если каждые чч:мм:сс
                if (_sp.getSelectedItemPosition() == 0)
                {
                    IntervalTextView.setText("Интервал (каждые чч:мм:сс):");
                    _AddItem.setVisibility(View.INVISIBLE);// Кнопка невидна
                    _ClearListButton.setVisibility(View.INVISIBLE);// Кнопка невидна
                    _list.setVisibility(View.INVISIBLE);// и список тоже
                    typeStart = 0;
                }
                else
                {
                    // если в указанное время
                    IntervalTextView.setText("Укажите время (чч:мм:сс):");
                    _AddItem.setVisibility(View.VISIBLE);// Кнопка видна
                    _ClearListButton.setVisibility(View.VISIBLE);// Кнопка видна
                    _list.setVisibility(View.VISIBLE);// и список тоже
                    typeStart = 1;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                //
            }
        });
    }

    public void onClick_StartButton(View view)
    {
        Intent stIntent = new Intent(this, BatteryService.class);

        TextView time = (TextView)findViewById(R.id.editTextTime);
        String time_text = time.getText().toString();

        if(typeStart == 0)
        {
            if (time_text.isEmpty())
            {
                AlertDialog.Builder dialog1 = new AlertDialog.Builder(this);
                dialog1.setMessage("Заполните поле Интервал");
                dialog1.setCancelable(false);
                AlertDialog.Builder ok = dialog1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = dialog1.create();
                alert.setTitle("Не заполнено поле");
                alert.show();

                return;
            }
            else
            {
                if(!IsCorrectTime(time_text))
                {
                    Toast incorrectTime = Toast.makeText(getApplicationContext(), "Введите корректно время\nНо не более 23:59:59", Toast.LENGTH_SHORT);
                    incorrectTime.setGravity(Gravity.CENTER, 0, 0);
                    incorrectTime.show();
                    return;
                }
                else
                {
                    param[5] = typeStart+"";
                    param[6] = time_text;
                }
            }
        }
        if(typeStart == 1)
        {
            if(_array.isEmpty())
            {
                Toast incorrectTime = Toast.makeText(getApplicationContext(), "Введите хотя бы одно значение", Toast.LENGTH_SHORT);
                incorrectTime.setGravity(Gravity.CENTER, 0, 0);
                incorrectTime.show();
                return;
            }
            else
            {
                param[5] = typeStart+"";
                param[6]= ArrayTimeToString(_array.toArray());
            }
        }

        _Filesystem.WriteParams(param);// Сохраняем настройки и параметры

        //Передаем в BatteryService параметры
        stIntent.putExtra("TextView_PARAM", param);

        startService(stIntent);

        Toast toast = Toast.makeText(getApplicationContext(), "Сервис запущен", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void onClick_StopButton(View view)
    {
        Intent stIntent = new Intent(this, BatteryService.class);
        if (stIntent != null)
        {
            stopService(stIntent);
            stIntent = null;

            Toast toast = Toast.makeText(getApplicationContext(), "Сервис остановлен", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    public void onClick_ButtonSetting(View view)
    {
        Intent MailIntent = new Intent(MainActivity.this, Activity_Mailsetting.class);
        //Передаем в BatteryService параметры
        MailIntent.putExtra("TextView_PARAM", param);
        startActivityForResult(MailIntent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        //super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2)
        {
            if (resultCode == RESULT_OK)
            {
                String[] eparam = data.getStringArrayExtra("MAIL_SETTING");
                param[0] = eparam[0];// Адрес
                param[1] = eparam[1];// Сервер
                param[2] = eparam[2];//Логин
                param[3] = eparam[3];//Пароль
                param[4] = eparam[4];//порт
            }
        }
    }
    // Добавление значения в список
    public void onClick_AddItem(View view)
    {
        Toast incorrectTime = Toast.makeText(getApplicationContext(), "Введите корректно время", Toast.LENGTH_SHORT);
        incorrectTime.setGravity(Gravity.CENTER, 0, 0);


        TextView _editText = (TextView)findViewById(R.id.editTextTime);
        String _field = _editText.getText().toString().trim();
        if (!_field.isEmpty())
        {
            if(IsCorrectTime(_field))
            {
                dataList.insert(_field, 0);// Добавляем значение в адаптер вверху
                dataList.notifyDataSetChanged();// Обновляем данные
                _editText.setText("");//очищаем поле ввода
            }
            else
            {
                incorrectTime.show();
            }
        }
    }
    // TODO: Очищает полностью список внесенных дат
    public void onClick_ClearListButton(View view)
    {
        dataList.clear();
        dataList.notifyDataSetChanged();// Обновляем данные
    }

    private boolean IsCorrectTime(String time)
    {
        Pattern p = Pattern.compile("\\d{2}:\\d{2}:\\d{2}");
        Matcher m = p.matcher(time);
        if(m.matches())
        {
            String[] inTime = time.split(":", 3);
            int hh = Integer.valueOf(inTime[0]), mm = Integer.valueOf(inTime[1]), ss = Integer.valueOf(inTime[1]);

            if(hh < 0 || mm < 0 || ss < 0)
                return false;
            if(hh > 23 || mm > 59 || ss > 59)
                return false;
            return true;
        }
        return false;
    }

    public String ArrayTimeToString(Object[] InTimes)
    {
        String _strToSave = "";
        for (Object _tmp : InTimes)
            _strToSave += _tmp.toString() + ";";
        return _strToSave.substring(0, _strToSave.length()-1);
    }
}
