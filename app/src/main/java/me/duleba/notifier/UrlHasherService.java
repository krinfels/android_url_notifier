package me.duleba.notifier;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import me.duleba.notifier.database.HashedUrl;
import me.duleba.notifier.database.UrlDatabase;

public class UrlHasherService extends JobService {
    final String CHANNEL_ID = "UrlHasherServiceChannel";

    private NotificationManager notificationManager;

    @Override
    public void onCreate() {
        this.notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        //Create notification channel
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, getString(R.string.notificationChannelId),
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription(getString(R.string.notificationChannelDesc));
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        new Thread(() -> processAll(params)).start();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }


    private void processAll(JobParameters params) {
        List<HashedUrl> dataSource = UrlDatabase.getDB(this.getApplicationContext()).urlDao().getAll();

        LinkedList<Thread> childrenList = new LinkedList<>();

        for (HashedUrl url : dataSource)
            //Only process site if it hasnt been checked during the last minute
            if (System.currentTimeMillis() / 100 - url.getLastChanged() > 60) {
                childrenList.add(new Thread(() -> processSingle(url)));
                childrenList.getLast().start();
            }

        while (childrenList.size() > 0) {
            while (childrenList.getFirst().isAlive()) {
                try {
                    childrenList.getFirst().join();
                } catch (InterruptedException e) {
                }
            }
            childrenList.pop();
        }

        jobFinished(params, false);
    }

    private void processSingle(HashedUrl url) {
        long hash = 0;
        try {
            hash = UrlHasher.hashUrl(url.getUrl());
        } catch (IOException e) {
            UrlDatabase.getDB(this.getApplicationContext())
                    .urlDao().setInvalid(url.getUrl());
            return;
        }

        if(url.getLastChanged() < 0 || hash != url.getHash()) {
            if(url.getLastChanged() != -2)
                sendNotification(url.getUrl(), hash);
            //lastChanged field is updated in query
            UrlDatabase.getDB(this.getApplicationContext())
                    .urlDao().updateHash(url.getUrl(), hash);
        }
    }

    private void sendNotification(String url, long hash) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.notify((int) hash, new Notification.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle(getString(R.string.notificationTitle))
                    .setContentText(url + " " + getString(R.string.notificationText))
                    .setChannelId(CHANNEL_ID)
                    .build());
        }else {
            notificationManager.notify((int) hash, new Notification.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle(getString(R.string.notificationTitle))
                    .setContentText(url + " " + getString(R.string.notificationText))
                    .build());
        }
    }
}
