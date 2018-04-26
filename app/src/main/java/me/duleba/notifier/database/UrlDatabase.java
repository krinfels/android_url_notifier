package me.duleba.notifier.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

@Database(entities = {HashedUrl.class}, version = 1, exportSchema = false)
public abstract class UrlDatabase extends RoomDatabase {
    public static UrlDatabase singleton;

    public abstract UrlDao urlDao();

    public static synchronized UrlDatabase getDB(Context context) {
        if(singleton == null)
            singleton = Room.databaseBuilder(context.getApplicationContext(), UrlDatabase.class, "UrlDB")
                            .allowMainThreadQueries().build();

        return singleton;
    }

    public static void closeDB() {
        if(singleton == null) return;

        singleton.close();
        singleton = null;
    }
}
