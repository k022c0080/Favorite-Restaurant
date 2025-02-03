package jp.ac.neec.it.k022c0080.k10_0080_finaltask

import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.appcompat.app.AppCompatActivity

class FavoriteShopActivity : AppCompatActivity() {
    private val _helper = DatabaseHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_shop)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val lvFavoriteList = findViewById<ListView>(R.id.lvfavoritelist)

        // データベースのインスタンスを取得
        val db = _helper.readableDatabase

        // データを取得するクエリ
        val cursor = db.query(
            "favoriteshops", // テーブル名
            arrayOf("ShopName", "ShopCategory", "ShopAddress", "ShopNote"), // 取得したい列
            null, // WHERE句
            null, // WHERE句に対応する引数
            null, // GROUP BY句
            null, // HAVING句
            null // ORDER BY句
        )

        // データをリストに変換
        val dataList: MutableList<Map<String, String>> = mutableListOf()
        while (cursor.moveToNext()) {
            val shopName = cursor.getString(cursor.getColumnIndexOrThrow("ShopName"))
            val shopCategory = cursor.getString(cursor.getColumnIndexOrThrow("ShopCategory"))
            val shopAddress = cursor.getString(cursor.getColumnIndexOrThrow("ShopAddress"))
            val shopNote = cursor.getString(cursor.getColumnIndexOrThrow("ShopNote"))
            val dataMap = mapOf(
                "shopname" to shopName,
                "shopcategory" to shopCategory,
                "shopaddress" to shopAddress,
                "shopnote" to shopNote
            )
            dataList.add(dataMap)
        }
        cursor.close()

        val from = arrayOf("shopname", "shopcategory")
        val to = intArrayOf(android.R.id.text1, android.R.id.text2)
        val adapter = SimpleAdapter(this, dataList, android.R.layout.simple_list_item_2, from, to)
        lvFavoriteList.adapter = adapter

        // リストビューにコンテキストメニューを登録
        registerForContextMenu(lvFavoriteList)
    }

    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_context_menu_list, menu)
    }


    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val lvFavoriteList = findViewById<ListView>(R.id.lvfavoritelist)
        val selectedData = (lvFavoriteList.adapter.getItem(info.position) as Map<String, String>)

        return when (item.itemId) {
            R.id.menuListContextExplainEdit -> {
                val intent = Intent(this, FavoriteAddActivity::class.java).apply {
                    putExtra("shopName", selectedData["shopname"])
                    putExtra("shopCategory", selectedData["shopcategory"])
                    putExtra("shopAddress", selectedData["shopaddress"])
                    putExtra("shopNote", selectedData["shopnote"])
                }
                startActivity(intent)
                true
            }
            R.id.menuListContextDelete -> {
                // データベースのインスタンスを取得
                val dbHelper = DatabaseHelper(this)
                val db = dbHelper.writableDatabase

                // 削除クエリの実行
                db.execSQL("DELETE FROM favoriteshops WHERE ShopName = ? AND ShopCategory = ? AND ShopAddress = ?", arrayOf(selectedData["shopname"], selectedData["shopcategory"], selectedData["shopaddress"]))

                // 削除後、再度リストを更新
                recreate()

                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        _helper.close()
        super.onDestroy()
    }
}
