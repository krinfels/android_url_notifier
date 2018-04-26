package me.duleba.notifier.database;

import android.arch.persistence.room.*;
import android.support.annotation.NonNull;

@Entity
public class HashedUrl {
    @NonNull
    @PrimaryKey
    private String url;

    @ColumnInfo(name = "hash")
    private long hash = 0;

    @ColumnInfo(name = "lastChanged")
    private long lastChanged = -1;

    @Override
    public boolean equals(Object other) {
        if(!getClass().equals(other.getClass()))
            return false;

        HashedUrl b = (HashedUrl)other;

        return url.equals(b.getUrl())
                && hash == b.getHash()
                && lastChanged == b.lastChanged;
    }

    public long getHash() {
        return hash;
    }

    public void setHash(long hash) {
        this.hash = hash;
    }

    public long getLastChanged() {
        return lastChanged;
    }

    public void setLastChanged(long lastChanged) {
        this.lastChanged = lastChanged;
    }

    @NonNull
    public String getUrl() {
        return url;
    }

    public void setUrl(@NonNull String url) {
        this.url = url;
    }
}
