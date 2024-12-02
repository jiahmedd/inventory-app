package cs477.gmu.jahmed9_project2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Arrays;

public class DatabaseOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 4;
    private static final String TABLE_NAME = "books";
    private static final String COL_ID = "_id";
    private static final String COL_ITEM = "item";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_PRICE = "price";
    private static final String COL_QUANTITY = "stock";

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
            COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_ITEM + " TEXT UNIQUE NOT NULL, " +
            COL_DESCRIPTION + " TEXT, " +
            COL_PRICE + " REAL, " +
            COL_QUANTITY + " INTEGER NOT NULL)";

    public DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        insertInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    private void insertInitialData(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();

        cv.put(COL_ITEM, "Medium Gold Hoop Earrings");
        cv.put(COL_DESCRIPTION, "14K gold medium-sized hoop earrings");
        cv.put(COL_PRICE, 75.00);
        cv.put(COL_QUANTITY, 10);
        db.insert(TABLE_NAME, null, cv);

        cv.put(COL_ITEM, "Small Gold Hoop Earrings");
        cv.put(COL_DESCRIPTION, "14k gold small-sized hoop earrings");
        cv.put(COL_PRICE, 50.00);
        cv.put(COL_QUANTITY, 13);
        db.insert(TABLE_NAME, null, cv);

        cv.put(COL_ITEM, "Medium Silver Hoop Earrings");
        cv.put(COL_DESCRIPTION, "Sterling-silver medium-sized hoop earrings");
        cv.put(COL_PRICE, 55.00);
        cv.put(COL_QUANTITY, 15);
        db.insert(TABLE_NAME, null, cv);

        cv.put(COL_ITEM, "Gold-Plated Pendant Necklace");
        cv.put(COL_DESCRIPTION, "14k gold plated over sterling silver chain with a heart-shaped pendant");
        cv.put(COL_PRICE, 45.00);
        cv.put(COL_QUANTITY, 7);
        db.insert(TABLE_NAME, null, cv);

        cv.put(COL_ITEM, "18k Gold Daily Stacker Ring");
        cv.put(COL_DESCRIPTION, "18K Gold, 1mm thick ring. Perfect addition to any ring stack!");
        cv.put(COL_PRICE, 80.00);
        cv.put(COL_QUANTITY, 7);
        db.insert(TABLE_NAME, null, cv);
    }

    public long insert(String item, String description, double price, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ITEM, item.trim());
        cv.put(COL_DESCRIPTION, description);
        cv.put(COL_PRICE, price);
        cv.put(COL_QUANTITY, quantity);
        return db.insert(TABLE_NAME, null, cv);
    }

    public int update(String item, String description, double price, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_DESCRIPTION, description);
        cv.put(COL_PRICE, price);
        cv.put(COL_QUANTITY, quantity);
        int rowsAffected = db.update(TABLE_NAME, cv, COL_ITEM + "=?", new String[]{item.trim()});
        return rowsAffected;
    }

    public int delete(String item) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, COL_ITEM + "=?", new String[]{item.trim()});
    }

    public Cursor readAll() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(
                TABLE_NAME,
                new String[]{COL_ID, COL_ITEM, COL_DESCRIPTION, COL_PRICE, COL_QUANTITY},
                null, null, null, null,
                COL_ITEM + " ASC"
        );
        return c;
    }
}
