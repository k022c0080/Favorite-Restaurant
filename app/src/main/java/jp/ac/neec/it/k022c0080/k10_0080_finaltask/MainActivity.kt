package jp.ac.neec.it.k022c0080.k10_0080_finaltask

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    companion object{
        private const val DEBUG_TAG = "FinalTask"

        private const val SHOPINFO_URL = "https://webservice.recruit.co.jp/hotpepper/shop/v1/?"
        private const val MY_API = "key="//ここにAPIを入れる
        private const val Keyword = "keyword="
        private const val format = "format=json"
    }

    private val _helper = DatabaseHelper(this@MainActivity)
    private var _list: MutableList<MutableMap<String,String>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val lvShopList = findViewById<ListView>(R.id.lvshoplist)
        lvShopList.onItemClickListener = ListItemClickListener()

    }

    @UiThread
    private fun receiveShopInfo (urlFull: String){
        val backgroundReceiver = ShopInfoBackgroundReceiver(urlFull)
        val executeService = Executors.newSingleThreadExecutor()
        val future = executeService.submit(backgroundReceiver)
        val result = future.get()
        showShopInfo(result)
    }

    @UiThread
    private fun showShopInfo(result: String) {
        if (result.isEmpty()) {
            Toast.makeText(this, getString(R.string.not_Data), Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val rootJSON = JSONObject(result)

            // エラーチェック
            if (rootJSON.has("results")) {
                val resultsObject = rootJSON.getJSONObject("results")

                if (resultsObject.has("error")) {
                    val errorArray = resultsObject.getJSONArray("error")
                    val errorMessage = errorArray.getJSONObject(0).getString("message")
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    return
                }

                // 通常の処理（結果を解析）
                val resultsAvailable = resultsObject.optString("results_available", "0")
                if (resultsAvailable == "0") {
                    Toast.makeText(this, getString(R.string.not_found), Toast.LENGTH_SHORT).show()
                    return
                }

                val shops = resultsObject.getJSONArray("shop")
                val lvShopList = findViewById<ListView>(R.id.lvshoplist)
                _list.clear()

                for (i in 0 until shops.length()) {
                    val shop = shops.getJSONObject(i)
                    val shopName = shop.getString("name")
                    val address = shop.getString("address")
                    val genreName = shop.getJSONObject("genre").getString("name")
                    val shopInfo = mutableMapOf("shopname" to shopName, "address" to address, "genreName" to genreName)
                    _list.add(shopInfo)
                }

                val from = arrayOf("shopname", "address", "genreName")
                val to = intArrayOf(android.R.id.text1, android.R.id.text2,android.R.id.text2)
                val adapter = SimpleAdapter(
                    this@MainActivity, _list, android.R.layout.simple_list_item_2, from, to
                )
                lvShopList.adapter = adapter
            } else {
                Toast.makeText(this, getString(R.string.error_search_message), Toast.LENGTH_SHORT).show()
            }

        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.miss_analysis), Toast.LENGTH_SHORT).show()
        }
    }



    private inner class ShopInfoBackgroundReceiver(url: String): Callable<String> {
        private val _url = url
        @WorkerThread
        override fun call(): String {
            var result = ""
            val url = URL(_url)
            val con = url.openConnection() as HttpURLConnection
            con.connectTimeout = 1000
            con.readTimeout = 1000
            con.requestMethod = "GET"
            try{
                con.connect()
                val stream = con.inputStream
                result = is2String(stream)
                stream.close()
            }
            catch (ex: SocketTimeoutException){
                Log.w(DEBUG_TAG,"通信タイムアウト",ex)
            }
            con.disconnect()
            return result
        }
    }

    private fun is2String(stream: InputStream): String{
        val sb = StringBuilder()
        val reader = BufferedReader(InputStreamReader(stream,StandardCharsets.UTF_8))
        var line = reader.readLine()
        while(line != null){
            sb.append(line)
            line = reader.readLine()
        }
        reader.close()
        return sb.toString()
    }
    fun btnSearchHistory(view: View){
        val intent = Intent(this@MainActivity,SearchHistoryActivity::class.java)
        startActivity(intent)
    }

    fun btnFavoriteShop(view: View){
        val intent = Intent(this@MainActivity,FavoriteShopActivity::class.java)
        startActivity(intent)
    }

    fun btnSearchBotton(view: View){
        val searchshop = findViewById<EditText>(R.id.etsearchshop)
        if (searchshop.text.toString().isEmpty()){
            Toast.makeText(this, getString(R.string.empty_error), Toast.LENGTH_SHORT).show()
        } else {
            // 入力がある場合の処理
            val input = searchshop.text.toString()
            val urlFull = "$SHOPINFO_URL$MY_API&$Keyword$input&$format"

            receiveShopInfo(urlFull)
        }
    }

    private inner class ListItemClickListener : AdapterView.OnItemClickListener{
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            if (_list.isEmpty()) {
                Toast.makeText(this@MainActivity, getString(R.string.list_free), Toast.LENGTH_SHORT).show()
                return
            }

            val db = _helper.writableDatabase
            val sqlInsert = "INSERT INTO SearchHistory(ShopName, ShopAddress, ShopCategory) VALUES(?, ?, ?)"
            val stmt = db.compileStatement(sqlInsert)

            val item = parent.getItemAtPosition(position) as Map<String, String>
            val shopName = item["shopname"]
            val address = item["address"]
            val genreName = item["genreName"]

            stmt.bindString(1, shopName)
            stmt.bindString(2, address)
            stmt.bindString(3, genreName)
            stmt.executeInsert()

            db.close()

            val intent = Intent(this@MainActivity,FavoriteAddActivity::class.java)
            intent.putExtra("shopName",shopName)
            intent.putExtra("address",address)
            intent.putExtra("genreName",genreName)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        _helper.close()
        super.onDestroy()
    }
}