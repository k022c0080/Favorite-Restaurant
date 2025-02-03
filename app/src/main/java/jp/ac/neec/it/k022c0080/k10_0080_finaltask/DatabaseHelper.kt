package jp.ac.neec.it.k022c0080.k10_0080_finaltask

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper (context: Context):SQLiteOpenHelper(context,DATABASE_NAME,null,DATABASE_VERSION){
    companion object{
        private const val DATABASE_NAME = "shop.db"
        private const val DATABASE_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase) {
        val sb = StringBuilder()
        sb.append("CREATE TABLE favoriteshops (")
        sb.append("_id INTEGER PRIMARY KEY,")
        sb.append("ShopName TEXT,")
        sb.append("ShopAddress TEXT,")
        sb.append("ShopCategory TEXT,")
        sb.append("ShopNote TEXT")
        sb.append(");")
        val favoritesql = sb.toString()

        val sh = StringBuilder()
        sh.append("CREATE TABLE SearchHistory (")
        sh.append("_id INTEGER PRIMARY KEY,")
        sh.append("ShopName TEXT,")
        sh.append("ShopAddress TEXT,")
        sh.append("ShopCategory TEXT")
        sh.append(");")
        val searchhistory = sh.toString()

        db.execSQL(favoritesql)
        db.execSQL(searchhistory)
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }
}