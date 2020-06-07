package org.rydalinc.fluttergetmessages;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Query;

import java.util.List;

@Entity
class message {
    @NonNull
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "timestamp")
    public String timestamp;
    @ColumnInfo(name = "message")
    public String message;
    @ColumnInfo(name = "read")
    public String read;


}


