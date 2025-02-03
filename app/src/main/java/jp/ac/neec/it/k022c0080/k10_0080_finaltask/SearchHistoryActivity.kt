package jp.ac.neec.it.k022c0080.k10_0080_finaltask

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SearchHistoryActivity : AppCompatActivity() {
    private val _helper = DatabaseHelper(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_history)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val lvShopHistory = findViewById<ListView>(R.id.lvshophistory)

        // データベースのインスタンスを取得
        val db = _helper.readableDatabase

        // データを取得するクエリ
        val cursor = db.query(
            "SearchHistory",
            arrayOf("ShopName", "ShopCategory", "ShopAddress"), // 取得したい列
            null, // WHERE句
            null, // WHERE句に対応する引数
            null, // GROUP BY句
            null, // HAVING句
            null // ORDER BY句
        )

        // データをリストに変換
        val dataList = mutableListOf<Map<String, String>>()
        while (cursor.moveToNext()) {
            val idxshopname = cursor.getColumnIndex("ShopName")
            val shopName = cursor.getString(idxshopname)
            val idxshopcategory = cursor.getColumnIndex("ShopCategory")
            val shopCategory = cursor.getString(idxshopcategory)

            val idxShopAddress = cursor.getColumnIndex("ShopAddress")
            val shopAddress = cursor.getString(idxShopAddress)
            val dataMap = mapOf(
                "shopname" to shopName,
                "shopcategory" to shopCategory,
                "address" to shopAddress,
            )
            dataList.add(dataMap)
        }
        cursor.close()

        // SimpleAdapterを使用してリストビューにデータを表示
        val from = arrayOf("shopname", "shopcategory")
        val to = intArrayOf(android.R.id.text1, android.R.id.text2)
        val adapter = SimpleAdapter(this, dataList, android.R.layout.simple_list_item_2, from, to)
        lvShopHistory.adapter = adapter

        lvShopHistory.onItemClickListener = ListItemClickListener()
    }

    private inner class ListItemClickListener : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            val item = parent.getItemAtPosition(position) as Map<String, String>
            val shopName = item["shopname"]
            val address = item["address"]
            val genreName = item["shopcategory"]

            // データベースのインスタンスを取得
            val dbHelper = DatabaseHelper(this@SearchHistoryActivity)
            val db = dbHelper.readableDatabase

            // お気に入りテーブルから該当するshopnameのデータを取得
            val cursor = db.query(
                "favoriteshops",
                arrayOf("ShopName", "ShopAddress", "ShopCategory", "ShopNote"),
                "ShopName = ?",
                arrayOf(shopName),
                null, null, null
            )

            val intent = Intent(this@SearchHistoryActivity, FavoriteAddActivity::class.java)
            if (cursor.moveToFirst()) {
                // 該当データが存在する場合、それを取得してインテントに追加
                val idxShopAddress = cursor.getColumnIndex("ShopAddress")
                val savedAddress = cursor.getString(idxShopAddress)

                val idxShopCategory = cursor.getColumnIndex("ShopCategory")
                val savedCategory = cursor.getString(idxShopCategory)

                val idxShopNote = cursor.getColumnIndex("ShopNote")
                val savedNote = cursor.getString(idxShopNote)

                intent.putExtra("shopName", shopName)
                intent.putExtra("address", savedAddress)
                intent.putExtra("genreName", savedCategory)
                intent.putExtra("shopNote", savedNote)
            } else {
                // 該当データが存在しない場合、渡された値をそのまま使用
                intent.putExtra("shopName", shopName)
                intent.putExtra("address", address)
                intent.putExtra("genreName", genreName)
            }
            cursor.close()
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var returnVal = true
        if (item.itemId == android.R.id.home) {
            finish()
        } else {
            returnVal = super.onOptionsItemSelected(item)
        }
        return returnVal
    }

    override fun onDestroy() {
        _helper.close()
        super.onDestroy()
    }
}
