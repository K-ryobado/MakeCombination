package com.ryocomp.makecombination;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TestOpenHelper extends SQLiteOpenHelper {

    // データーベースのバージョン
    private static final int DATABASE_VERSION = 1;

    // データーベース名
    private static final String DATABASE_NAME = "makecombination.db";
    private static final String TABLE_NAME = "main";
    private static final String TABLE_NAME2 = "setting";
    private static final String _ID = "_id";
    private static final String COLUMN_NAME_TITLE = "number";
    private static final String COLUMN_NAME_SUBTITLE = "member";
    private static final String COLUMN_NAME_ELEMENT = "sex";
    private static final String COLUMN_NAME_ELEMENT2 = "pair";
    private static final String COLUMN_NAME_ELEMENT3 = "rest";
    private static final String COLUMN_NAME_ELEMENT4 = "enable";
    private static final String COLUMN_NAME_COURT = "courtNumber";
    private static final String COLUMN_NAME_MIX = "mixOnly";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY," +
                    COLUMN_NAME_TITLE + " INTEGER," +
                    COLUMN_NAME_SUBTITLE + " TEXT,"+
                    COLUMN_NAME_ELEMENT + " TEXT,"+
                    COLUMN_NAME_ELEMENT2 + " TEXT,"+
                    COLUMN_NAME_ELEMENT3 + " TEXT,"+
                    COLUMN_NAME_ELEMENT4 + " TEXT)";

    private static final String SQL_CREATE_ENTRIES2 =
            "CREATE TABLE " + TABLE_NAME2 + " (" +
                    COLUMN_NAME_COURT + " TEXT," +
                    COLUMN_NAME_MIX + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;


    TestOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // テーブル作成
        // SQLiteファイルがなければSQLiteファイルが作成される
        Log.d("debug",SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
        Log.d("debug",SQL_CREATE_ENTRIES2);
        db.execSQL(SQL_CREATE_ENTRIES2);

        Log.d("debug", "onCreate(SQLiteDatabase db)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // アップデートの判別
        db.execSQL(
                SQL_DELETE_ENTRIES
        );
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}