package me.duleba.notifier.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.net.URL;
import java.util.List;

@Dao
public interface UrlDao {
    @Query("Select * FROM hashedurl ORDER BY lastChanged ASC")
    List<HashedUrl> getAll();

    @Query("Select * FROM hashedurl ORDER BY lastChanged ASC " +
            "LIMIT 1 OFFSET :pos")
    List<HashedUrl> getSingle(int pos);

    @Query("SELECT * FROM hashedurl ORDER BY lastChanged ASC")
    LiveData<List<HashedUrl>> getObservable();

    @Query("UPDATE hashedurl " +
            "SET hash = :newHash, lastChanged = strftime('%s', 'now') " +
            "WHERE url = :url")
    void updateHash(String url, long newHash);

    @Query("UPDATE hashedurl " +
            "SET lastChanged = -2 " +
            "WHERE url = :url")
    void setInvalid(String url);

    @Query("SELECT COUNT(*) FROM hashedurl")
    int getSize();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(HashedUrl... urls);

    @Delete
    void delete(HashedUrl entry);

    @Query("DELETE FROM hashedurl")
    void purge();
}
