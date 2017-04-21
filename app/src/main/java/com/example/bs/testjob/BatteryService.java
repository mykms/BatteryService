package com.example.bs.testjob;

import android.app.Service;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.os.Build;
import android.util.Log;
import android.support.v4.app.*;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;
import java.util.Calendar;
import java.util.Properties;

import javax.mail.*;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class BatteryService extends Service
{
    private short typeStart;
    private Timer CurTime, reSendTimer;
    private Intent MainIntent;
    private String[] MailParam;
    private PowerManager.WakeLock wakeLock;
    public FileJob _FileSupport;

    public BatteryService()
    {
        //
    }
    @Override
    public void onCreate()
    {
        _FileSupport = new FileJob(getApplicationContext());

        Notification.Builder builder = new Notification.Builder(this);
                //.setSmallIcon(R.mipmap.ic_launcher);

        Notification notification;
        if (Build.VERSION.SDK_INT < 16)
            notification = builder.getNotification();
        else
            notification = builder.build();

        startForeground(777, notification);

        Intent hideIntent = new Intent(this, HideNotificationService.class);
        startService(hideIntent);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //Log.d("BatteryService", "Service: onStartCommand");
        MainIntent = intent;
        // Получаем параметры
        MailParam = intent.getStringArrayExtra("TextView_PARAM");
        typeStart = Short.valueOf(MailParam[5]);// Тип запуска

        RunJob();

        return START_STICKY;
    }
    @Override
    public void onDestroy()
    {
        //Log.d("BatteryService", "Service: onDestroy");
        if (CurTime != null)
        {
            CurTime.cancel();
            CurTime = null;
        }
        if (wakeLock != null)
            wakeLock.release();
    }
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
    @Override
    public void onTaskRemoved(Intent rootIntent)
    {
        //Log.d("BatteryService", "Service: onTaskRemoved");
    }

    /**
     * Получает текущее время
     * @return возвращает время в формате мм:чч:сс
     */
    private String CurTime()
    {
        // Берем дату и время с системного календаря:
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("kk:mm:ss");
        String _time = simpleDateFormat.format(calendar.getTime());
        if (Integer.valueOf(_time.substring(0, 2)) == 24)
        {
            return "00" + _time.substring(2);
        }

        return _time;
    }

    private void ResendInfo()
    {
        // Если сервис работает и требуется отправка
        reSendTimer = new Timer();
        TimerTask secondSend = new TimerTask()
        {
            @Override
            public void run()
            {
                // Основной метод отправки
                if (isNetworkActive())
                {
                    String curstr = _FileSupport.ReadData();
                    if (curstr.isEmpty())
                    {
                        // Если файл пустой, то закончим работу
                        if (reSendTimer != null) {
                            reSendTimer.cancel();
                            reSendTimer = null;
                        }
                    }
                    else
                    {
                        String[] FullArr = curstr.split("\n");
                        for (int i = 0; i < FullArr.length; i++)
                        {
                            // Отправляем письмо
                            if (SendInfoToEmail(MailParam, FullArr[i].split(";")[0], FullArr[i].replace(";", "\n")))
                            {
                                // Удалим из файла отправленные данные
                                StringBuilder newData = new StringBuilder();

                                for (int j = i+1; j < FullArr.length; j++)
                                {
                                    if (j >= FullArr.length)
                                    {
                                        newData = null;
                                        break;
                                    }
                                    else
                                        newData.append(FullArr[j]+"\n");
                                }
                                _FileSupport.WriteData(newData.toString(), false);
                            }
                        }
                    }
                }
            }
        };
        reSendTimer.schedule(secondSend, 0, 3*1000);
    }
    private void RunJob()
    {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Battery wakelook");
        wakeLock.acquire();

        CurTime = new Timer();

        TimerTask batTask = new BatteryTask();
        if(typeStart == 0)
        {
            String val_0 = MailParam[6];
            int hh = Integer.valueOf(val_0.split(":")[0]);
            int mm = Integer.valueOf(val_0.split(":")[1]);
            int ss = Integer.valueOf(val_0.split(":")[2]);
            long sec = (hh*60 + mm)*60 + ss;

            CurTime.schedule(batTask, 0, sec * 1000);
        }
        if(typeStart == 1)
        {
            CurTime.schedule(batTask, 0, 1 * 1000);// Каждую секунду мониторим время из списка
        }
    }
    // TODO: Возвращает информацию о батарее
    private String GetBatteryInfo()
    {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryIntent = registerReceiver(null, ifilter);
        String batLevel = "Уровень заряда: " + batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) + "% из " + batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, 100) + "%";
        String baTemp = "Температура: " + batteryIntent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
        String batVolt = "Напряжение: " + batteryIntent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
        String batHealth = "Состояние батареи: ";
        String batStatus = "Состояние заряда аккумулятора: ";
        String batIs = "Наличие батареи в устройстве: " + batteryIntent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, true);

        switch (batteryIntent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0))
        {
            case BatteryManager.BATTERY_HEALTH_DEAD:
            {
                batHealth += "батарея неработоспособна";
                break;
            }
            case BatteryManager.BATTERY_HEALTH_GOOD:
            {
                batHealth += "батарея в хорошем состоянии";
                break;
            }
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
            {
                batHealth += "батарея перегрета";
                break;
            }
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
            {
                batHealth += "у батареи повышенное напряжение";
                break;
            }
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
            {
                batHealth += "батарея неисправна";
                break;
            }
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
            {
                batHealth += "состояние неизвестно";
                break;
            }
            default: batHealth += "состояние неизвестно"; break;
        }
        switch (batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, 0))
        {
            case BatteryManager.BATTERY_STATUS_CHARGING:
            {
                batStatus += "батарея заряжается";
                break;
            }
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
            {
                batStatus += "батарея разряжена";
                break;
            }
            case BatteryManager.BATTERY_STATUS_FULL:
            {
                batStatus += "батарея заряжена";
                break;
            }
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
            {
                batStatus += "батарея не заряжается";
                break;
            }
            case BatteryManager.BATTERY_STATUS_UNKNOWN:
            {
                batStatus += "состояние неизвестно";
                break;
            }
        }

        return batLevel + "\n" + baTemp + "\n"+ batVolt + "\n" + batHealth + "\n" + batStatus + "\n" + batIs;
    }
    // Сбор и отправка информации
    private void CompileInfo()
    {
        String curTime = CurTime();
        if(typeStart == 0)
        {
            final String info = GetBatteryInfo();// Получаем информацию о батарее
            if (isNetworkActive())
            {
                boolean result = SendInfoToEmail(MailParam, "Информация о батарее " + curTime, info);// Отправляем письмо
            }
            else
            {
                // Сохраняем на диск
                _FileSupport.WriteData("Информация о батарее " + curTime + ";" + info.replace("\n", ";") + "\n", true);
                if (reSendTimer == null)
                {
                    ResendInfo();
                }
            }
        }
        if(typeStart == 1)
        {
            String[] val_1 = MailParam[6].split(";");

            for (int i = 0; i < val_1.length; i++)
            {
                // Если нашли время из списка и Текущее время содержится в нем
                if (curTime.compareToIgnoreCase(val_1[i]) == 0)
                {
                    final String info = GetBatteryInfo();// Получаем информацию о батарее
                    if (isNetworkActive())
                    {
                        boolean result = SendInfoToEmail(MailParam, "Информация о батарее " + curTime, info);// Отправляем письмо
                    }
                    else
                    {
                        // Сохраняем на диск
                        _FileSupport.WriteData("Информация о батарее " + curTime + ";" + info.replace("\n", ";") + "\n", true);
                        if (reSendTimer == null)
                        {
                            ResendInfo();
                        }
                    }
                    break;
                }
            }
        }
    }

    private class BatteryTask extends TimerTask
    {
        @Override
        public void run()
        {
            CompileInfo();
        }
    }
    // Отправка информации по smtp протоколу
    private boolean SendInfoToEmail(String[] params, String _subject, String _text)
    {
        String to = params[0];//"test201701@yandex.ru";//e-mail
        String host = params[1];//"smtp.yandex.com";//smtp host;
        final String user = params[2];//"test201701";//login;
        final String password = params[3];//"123456789q";//password;

        int port = Integer.valueOf(params[4]);//smtp port(456);

        // Сюда необходимо подставить адрес получателя сообщения
        String from = to;

        // Создание свойств, получение сессии
        Properties props = new Properties();

        // При использовании статического метода Transport.send() необходимо указать через какой хост будет передано сообщение
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.ssl.enable", "true");  // Если почтовый сервер использует SSL
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.put("mail.smtp.ssl.trust", "*");      // Если требуется сертификат
        props.put("mail.smtp.port", port);          // Указываем порт SMTP сервера.
        props.put("mail.smtp.auth", "true");        // Большинство SMTP серверов, используют авторизацию.
        props.put("mail.debug", "false");           // использовалось для отладки

        props.put("mail.smtp.connectiontimeout", 10*1000);
        props.put("mail.smtp.timeout", 10*1000);
        // Авторизируемся.
        Session session = Session.getDefaultInstance(props, new Authenticator()
        {
            // Указываем логин пароль, от почты, с которой будем отправлять сообщение.
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        try
        {
            // Создание объекта сообщения
            Message msg = new MimeMessage(session);

            // Установка атрибутов сообщения
            msg.setFrom(new InternetAddress(from));
            InternetAddress[] address = {new InternetAddress(to)};
            msg.setRecipients(Message.RecipientType.TO, address);
            msg.setSubject(_subject);
            msg.setSentDate(new Date());

            // Установка тела сообщения
            msg.setText(_text);

            // Отправка сообщения
            //Transport.send(msg);


            Transport transport = session.getTransport("smtp");
            try
            {
                transport.connect();
                transport.sendMessage(msg, msg.getAllRecipients());
            }
            catch (MessagingException e)
            {
                e.getStackTrace();
                return false;
            }
            finally
            {
                transport.close();
                return true;
            }

        }
        catch (MessagingException mex)
        {
            // Печать информации об исключении в случае его возникновения
            mex.printStackTrace();
            return false;
        }
    }

    private boolean isNetworkActive()
    {
        ConnectivityManager connetInet = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = connetInet.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable && connetInet.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }
}