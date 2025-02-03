package jp.ac.neec.it.k022c0080.k10_0080_finaltask

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class FavoriteAddActivity : AppCompatActivity() {
    private val _helper = DatabaseHelper(this@FavoriteAddActivity)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_add)
        val shopname = intent.getStringExtra("shopName")
        val address = intent.getStringExtra("address")
        val genreName = intent.getStringExtra("genreName")
        val shopNote = intent.getStringExtra("shopNote")

        val tvshopname = findViewById<TextView>(R.id.datashopname)
        val tvshopaddress = findViewById<TextView>(R.id.datashopaddress)
        val tvshopcategory = findViewById<TextView>(R.id.datashopcategory)
        val shopnote = findViewById<EditText>(R.id.etreview)

        tvshopname.text = shopname
        tvshopcategory.text = genreName
        tvshopaddress.text = address
        shopnote.setText(shopNote)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    fun btnfavoriteadd(view: View) {
        val review = findViewById<EditText>(R.id.etreview)
        if (review.text.isEmpty()) {
            Toast.makeText(this, getString(R.string.review_write), Toast.LENGTH_SHORT).show()
        } else {
            val db = _helper.writableDatabase

            // Intentから渡された値を取得
            val shopName = intent.getStringExtra("shopName")
            val address = intent.getStringExtra("address")
            val genreName = intent.getStringExtra("genreName")
            val shopNote = review.text.toString()

            // データが存在するか確認するクエリ
            val cursor = db.query(
                "favoriteshops",
                arrayOf("ShopName"),
                "ShopName = ?",
                arrayOf(shopName),
                null, null, null
            )

            if (cursor.moveToFirst()) {
                // 存在する場合はUPDATE文でShopNoteを更新
                val sqlUpdate = "UPDATE favoriteshops SET ShopNote = ? WHERE ShopName = ?"
                val stmt = db.compileStatement(sqlUpdate)
                stmt.bindString(1, shopNote)
                stmt.bindString(2, shopName)
                stmt.executeUpdateDelete()
            } else {
                // 存在しない場合はINSERT文で新規に追加
                val sqlInsert = "INSERT INTO favoriteshops (ShopName, ShopAddress, ShopCategory, ShopNote) VALUES(?, ?, ?, ?)"
                val stmt = db.compileStatement(sqlInsert)
                stmt.bindString(1, shopName)
                stmt.bindString(2, address)
                stmt.bindString(3, genreName)
                stmt.bindString(4, shopNote)
                stmt.executeInsert()
            }

            cursor.close()
            Toast.makeText(this, getString(R.string.done_add_favorite), Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var returnVal = true
        if(item.itemId == android.R.id.home){
            finish()
        }
        else{
            returnVal = super.onOptionsItemSelected(item)
        }
        return returnVal
    }

    override fun onDestroy() {
        _helper.close()
        super.onDestroy()
    }
}
