package com.example.mapwithmarker;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class Database extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "test.db";
    public static final String DB_TABLE_NAME = "test1";
    SQLiteDatabase db;

    public Database(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "CREATE TABLE "+DB_TABLE_NAME+" (lat decimal(19,5), lng decimal(19,5));"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(
                "DROP TABLE IF EXISTS "+DB_TABLE_NAME
        );
    }

    public void execSQL(String sql){
        db = this.getWritableDatabase();
        db.execSQL(sql);
    }

    public Cursor querySQL(String sql){
        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        return cursor;
    }
}
