package com.example.mapwithmarker;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class Database extends SQLiteOpenHelper {

    private static final int DB_VERSION = 3;
    private static final String DB_NAME = "playMap3v.db";
    public static final String DB_TABLE_NAME = "markers";
    SQLiteDatabase db;

    public Database(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE "+DB_TABLE_NAME+" ("
                +"lat decimal(19,5) not null,"
                +"lng decimal(19,5) not null,"
                +"name varchar(20),"
                +"description varchar(200),"
                +"primary key(lat,lng)"
                +");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(
                "DROP TABLE IF EXISTS "+DB_TABLE_NAME
        );
    }

    /*
        Database db = new Database(MainActivity.this);
        db.execSQL("INSERT ~");
        db.close();

        위와 같이 3줄로 SQL 명령어를 실행할 수 있도록 메소드를 만들었습니다.
     */
    public void execSQL(String sql){
        db = this.getWritableDatabase();
        db.execSQL(sql);
    }

    /*
        Database db = new Database(MainActivity.this);
        Cursor cursor = db.querySQL("SELECT ~");
        db.close();

        위와 같이 3줄로 SQL 명령어를 실행할 수 있도록 메소드를 만들었습니다.
        querySQL은 SELECT문과 같이 커서를 반환하는 명령어를 실행할 때 쓰입니다.
    */
    public Cursor querySQL(String sql){
        db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        return cursor;
    }
}
