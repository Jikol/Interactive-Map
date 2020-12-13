package com.example.interactivemap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class Database extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "schema.db";

    public static final String MEMBER_TABLE = "MEMBER";
    public static final String TEAM_TABLE = "TEAM";

    public static final String MEMBER_ID = "member_id";
    public static final String MEMBER_NAME = "name";
    public static final String MEMBER_PASSWORD = "password";
    public static final String MEMBER_FOCUS = "focus";

    public static final String TEAM_ID = "team_id";
    public static final String TEAM_NAME = "name";
    public static final String TEAM_SPEC = "spec";

    private Context context = com.example.interactivemap.Context.getContext();

    public Database(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTeam = "CREATE TABLE IF NOT EXISTS " + TEAM_TABLE + "(\n" +
                "\t\"team_id\"\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                "\t\"name\"\tTEXT NOT NULL,\n" +
                "\t\"spec\"\tTEXT NOT NULL\n" +
                ");";
            String createMember = "CREATE TABLE IF NOT EXISTS " + MEMBER_TABLE + "(\n" +
                "\t\"member_id\"\tINTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,\n" +
                "\t\"name\"\tTEXT NOT NULL,\n" +
                "\t\"password\"\tTEXT NOT NULL,\n" +
                "\t\"focus\"\tTEXT NOT NULL,\n" +
                "\t\"team_id\"\tINTEGER,\n" +
                "\tFOREIGN KEY(\"team_id\") REFERENCES \"TEAM\"(\"team_id\")\n" +
                ");";
        db.execSQL(createTeam);
        db.execSQL(createMember);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MEMBER_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + TEAM_TABLE);
        onCreate(db);
    }

    public boolean insertMember(Member member) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MEMBER_NAME, member.getName());
        contentValues.put(MEMBER_PASSWORD, member.getPassword());
        contentValues.put(MEMBER_FOCUS, member.getFocus());

        if (db.insert(MEMBER_TABLE, null, contentValues) >= 1) {
            return true;
        } else {
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Cursor getMember(Member member) {
        SQLiteDatabase db = this.getReadableDatabase();
        String retrieveSql = "SELECT m.member_id, m.name, m.focus\n" +
                "FROM MEMBER m\n" +
                "WHERE m.name = " + member.getName() + " AND m.password = " + member.getHashedPassword();
        return db.rawQuery(retrieveSql, null);
    }
}
