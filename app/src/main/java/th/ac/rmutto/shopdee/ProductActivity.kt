package th.ac.rmutto.shopdee

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.squareup.picasso.Picasso
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class ProductActivity : AppCompatActivity() {
    var imageViewFile: ImageView? = null
    var txtProductName: TextView? = null
    var txtPrice: TextView? = null
    var txtProductDetail: TextView? = null
    var quantity = 0
    var custID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_product)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //For an synchronous task
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        //Receive variables from caller
        val productID = intent.extras!!.getString("productID")

        val sharedPrefer = getSharedPreferences(
            "appPrefer", Context.MODE_PRIVATE)
        custID = sharedPrefer?.getString("custIDPref", null)


        imageViewFile = findViewById(R.id.imageViewFile)
        txtProductName = findViewById(R.id.txtProductName)
        txtPrice = findViewById(R.id.txtPrice)
        txtProductDetail = findViewById(R.id.txtProductDetail)

        val editQuantity: EditText = findViewById(R.id.editQuantity)
        val btnRemoveProduct: Button = findViewById(R.id.btnRemoveProduct)
        val btnAddProduct: Button = findViewById(R.id.btnAddProduct)
        val btnAddToCart: Button = findViewById(R.id.btnAddToCart)

        showProductDetail(productID)

        btnRemoveProduct!!.setOnClickListener {
            var lQuantity = editQuantity!!.text.toString().toInt()

            lQuantity -= 1

            if (lQuantity == 0) lQuantity = 1
            editQuantity!!.setText(lQuantity.toString())
        }

        btnAddProduct!!.setOnClickListener {
            var uQuantity = editQuantity!!.text.toString().toInt()

            uQuantity += 1

            if (uQuantity >= quantity) uQuantity = quantity
            editQuantity!!.setText(uQuantity.toString())
        }

        btnAddToCart!!.setOnClickListener {
            addCart(custID!!, productID!!, editQuantity.text.toString(), txtPrice?.text.toString())
            val refresh = Intent(this, MainActivity::class.java)
            startActivity(refresh)
            finish()

        }


    }

    private fun addCart(custID: String, productID: String, quantity: String, price: String){
        val url = getString(R.string.root_url) + getString(R.string.makeorder_url)

        val okHttpClient = OkHttpClient()
        val formBody: RequestBody = FormBody.Builder()
            .add("custID",custID)
            .add("productID",productID)
            .add("quantity",quantity)
            .add("price",price)
            .build()
        val request: Request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()
        try{
            val response = okHttpClient.newCall(request).execute()
            if(response.isSuccessful){
                val obj = JSONObject(response.body!!.string())
                val message = obj["message"].toString()
                val status = obj["status"].toString()

                if (status == "true") {
                    Toast.makeText(this, "เพิ่มสินค้าเข้าตะกร้าเรียบร้อยแล้ว", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }

            }else{
                response.code
                Toast.makeText(this, "ไม่สามารถเชื่อต่อกับเซิร์ฟเวอร์ได้", Toast.LENGTH_LONG)
            }
        }catch (e: IOException){
            e.printStackTrace()
        }
    }

    private fun showProductDetail(productID: String?)
    {
        val url: String = getString(R.string.root_url) + getString(R.string.product_url) + productID

        val okHttpClient = OkHttpClient()
        val request: Request = Request.Builder()
            .url(url)
            .get()
            .build()
        try {
            val response = okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                try {
                    val data = JSONObject(response.body!!.string())
                    if (data.length() > 0) {
                        var url = getString(R.string.root_url) +
                                getString(R.string.product_image_url) + data.getString("imageFile")

                        Picasso.get().load(url
                        ).into(imageViewFile)
                        txtProductName?.text = data.getString("productName")
                        txtPrice?.text = data.getString("price")
                        txtProductDetail?.text = data.getString("productDetail")
                        quantity = data.getString("quantity").toInt()

                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            } else {
                response.code
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}