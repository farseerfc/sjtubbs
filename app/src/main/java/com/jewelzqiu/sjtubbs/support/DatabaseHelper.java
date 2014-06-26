package com.jewelzqiu.sjtubbs.support;

import com.jewelzqiu.sjtubbs.main.BBSApplication;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * Created by jewelzqiu on 6/11/14.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int CURRENT_VERSION = 2;

    private static final String DB_NAME = "db_bbs";

    // Table frequent
    private static final String TB_FREQUENT = "frequent";

    public static final String COL_TITLE = "title";

    public static final String COL_NAME = "_id";

    public static final String COL_URL = "url";

    public static final String COL_COUNT = "count";

    public static final String COL_TIME = "time";

    // table section
    private static final String TB_SECTIONS = "sections";

    public static final String COL_SECTION_NAME = "_id";

    public static final String COL_SECTION_URL = "url";

    public static final String COL_SECTION_BOARD_LIST = "boardlist";

    // table board
    private static final String TB_BOARDS = "boards";

    public static final String COL_BOSRD_NAME = "_id";

    public static final String COL_BOSRD_TITLE = "title";

    public static final String COL_BOARD_URL = "url";

    public static final String COL_SUB_BOARD_LIST = "subboardlist";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, CURRENT_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TB_FREQUENT + "("
                + COL_NAME + " VARCHAR PRIMARY KEY, "
                + COL_TITLE + " VARCHAR, "
                + COL_URL + " VARCHAR, "
                + COL_COUNT + " INTEGER, "
                + COL_TIME + " LONG"
                + ")";
        db.execSQL(sql);

        sql = "CREATE TABLE IF NOT EXISTS " + TB_SECTIONS + "("
                + COL_SECTION_NAME + " VARCHAR PRIMARY KEY, "
                + COL_SECTION_URL + " VARCHAR, "
                + COL_SECTION_BOARD_LIST + " VARCHAR"
                + ")";
        db.execSQL(sql);

        sql = "CREATE TABLE IF NOT EXISTS " + TB_BOARDS + "("
                + COL_BOSRD_NAME + " VARCHAR PRIMARY KEY, "
                + COL_BOSRD_TITLE + " VARCHAR, "
                + COL_BOARD_URL + " VARCHAR, "
                + COL_SUB_BOARD_LIST + " VARCHAR"
                + ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 1:
                String sql = "CREATE TABLE IF NOT EXISTS " + TB_SECTIONS + "("
                        + COL_SECTION_NAME + " VARCHAR PRIMARY KEY, "
                        + COL_SECTION_URL + " VARCHAR, "
                        + COL_SECTION_BOARD_LIST + " VARCHAR"
                        + ")";
                db.execSQL(sql);

                sql = "CREATE TABLE IF NOT EXISTS " + TB_BOARDS + "("
                        + COL_BOSRD_NAME + " VARCHAR PRIMARY KEY, "
                        + COL_BOSRD_TITLE + " VARCHAR, "
                        + COL_BOARD_URL + " VARCHAR, "
                        + COL_SUB_BOARD_LIST + " VARCHAR"
                        + ")";
                db.execSQL(sql);
                break;
        }
    }

    public void insert(Board board) {
        SQLiteDatabase db = getWritableDatabase();
        String sql = "SELECT * FROM " + TB_FREQUENT + " WHERE "
                + COL_NAME + "=\"" + board.name + "\"";
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.getCount() == 0) {
            sql = "INSERT INTO " + TB_FREQUENT + " VALUES (\""
                    + board.name + "\", \"" + board.title + "\", \"" + board.url + "\", "
                    + 1 + ", " + System.currentTimeMillis() + ")";
        } else {
            sql = "UPDATE " + TB_FREQUENT + " SET "
                    + COL_COUNT + "=" + COL_COUNT + "+1, "
                    + COL_TIME + "=" + System.currentTimeMillis()
                    + " WHERE " + COL_NAME + "=\"" + board.name + "\"";
        }
        db.execSQL(sql);
        db.close();
    }

    public Cursor query() {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM " + TB_FREQUENT + " ORDER BY "
                + COL_COUNT + " DESC, " + COL_TIME + " ASC LIMIT 10";
        return db.rawQuery(sql, null);
    }

    public void updateSections(ArrayList<Section> sections) {
        SQLiteDatabase db = getWritableDatabase();

        // clear table
        String sql = "DROP TABLE " + TB_SECTIONS;
        db.execSQL(sql);
        sql = "DROP TABLE " + TB_BOARDS;
        db.execSQL(sql);

        sql = "CREATE TABLE IF NOT EXISTS " + TB_SECTIONS + "("
                + COL_SECTION_NAME + " VARCHAR PRIMARY KEY, "
                + COL_SECTION_URL + " VARCHAR, "
                + COL_SECTION_BOARD_LIST + " VARCHAR"
                + ")";
        db.execSQL(sql);
        sql = "CREATE TABLE IF NOT EXISTS " + TB_BOARDS + "("
                + COL_BOSRD_NAME + " VARCHAR PRIMARY KEY, "
                + COL_BOSRD_TITLE + " VARCHAR, "
                + COL_BOARD_URL + " VARCHAR, "
                + COL_SUB_BOARD_LIST + " VARCHAR"
                + ")";
        db.execSQL(sql);

        // insert data
        for (Section section : sections) {
            insertSection(db, section);
        }
        db.close();
    }

    private void insertSection(SQLiteDatabase db, Section section) {
        ArrayList<Board> boards = section.boardList;
        StringBuilder builder = new StringBuilder();
        for (Board board : boards) {
            insertBoard(db, board);
            if (builder.length() == 0) {
                builder.append(board.name);
            } else {
                builder.append(" " + board.name);
            }
        }
        String sql = "INSERT INTO " + TB_SECTIONS + " VALUES (\"" +
                section.name + "\", \"" + section.url + "\", \"" + builder.toString() + "\")";
        db.execSQL(sql);
    }

    private void insertBoard(SQLiteDatabase db, Board board) {
        StringBuilder builder = new StringBuilder();
        if (board.hasSubBoard) {
            for (Board subBoard : board.subBoardList) {
                insertBoard(db, subBoard);
                if (builder.length() == 0) {
                    builder.append(subBoard.name);
                } else {
                    builder.append(" " + subBoard.name);
                }
            }
        }

        String sql = "INSERT INTO " + TB_BOARDS + " VALUES (\"" +
                board.name + "\", \"" + board.title + "\", \"" + board.url + "\", \"" +
                builder.toString() + "\")";
        db.execSQL(sql);
    }

    public ArrayList<Section> getSectionList() {
        ArrayList<Section> result = new ArrayList<Section>();
        SQLiteDatabase db = getWritableDatabase();

        BBSApplication.boardNameList.clear();
        BBSApplication.boardMap.clear();
        HashMap<String, Board> map = getBoardList(db);

        Cursor cursor = db.rawQuery("SELECT * FROM " + TB_SECTIONS, null);
        if (cursor.getCount() == 0) {
            db.close();
            return result;
        }
        String name, url, boards;
        StringTokenizer tokenizer;
        ArrayList<Board> boardList;
        while (cursor.moveToNext()) {
            name = cursor.getString(cursor.getColumnIndex(COL_SECTION_NAME));
            url = cursor.getString(cursor.getColumnIndex(COL_SECTION_URL));
            boards = cursor.getString(cursor.getColumnIndex(COL_SECTION_BOARD_LIST));
            tokenizer = new StringTokenizer(boards);
            boardList = new ArrayList<Board>();
            while (tokenizer.hasMoreTokens()) {
                boardList.add(map.get(tokenizer.nextToken()));
            }
            result.add(new Section(name, url, boardList));
        }

        db.close();
        return result;
    }

    private HashMap<String, Board> getBoardList(SQLiteDatabase db) {
        HashMap<String, Board> result = new HashMap<String, Board>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TB_BOARDS, null);
        if (cursor.getCount() == 0) {
            return result;
        }
        String name, title, url, subList;
        StringTokenizer tokenizer;
        Board board;
        ArrayList<Board> subBoardList;
        while (cursor.moveToNext()) {
            name = cursor.getString(cursor.getColumnIndex(COL_BOSRD_NAME));
            title = cursor.getString(cursor.getColumnIndex(COL_BOSRD_TITLE));
            url = cursor.getString(cursor.getColumnIndex(COL_BOARD_URL));
            subList = cursor.getString(cursor.getColumnIndex(COL_SUB_BOARD_LIST));
            tokenizer = new StringTokenizer(subList);
            if (tokenizer.countTokens() == 0) {
                board = new Board(title, name, url);
                BBSApplication.boardMap.put(title, board);
                BBSApplication.boardMap.put(name, board);
                BBSApplication.boardNameList.add(title);
                BBSApplication.boardNameList.add(name);
            } else {
                subBoardList = new ArrayList<Board>();
                while (tokenizer.hasMoreTokens()) {
                    String subBoardName = tokenizer.nextToken();
                    subBoardList.add(BBSApplication.boardMap.get(subBoardName));
                }
                board = new Board(title, name, url, subBoardList);
            }
            result.put(name, board);
        }

        return result;
    }
}
