package com.ifcodedeveloper.cakwangcafe.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ifcodedeveloper.cakwangcafe.ItemClickSupport;
import com.ifcodedeveloper.cakwangcafe.R;
import com.ifcodedeveloper.cakwangcafe.adapter.CartAdapter;
import com.ifcodedeveloper.cakwangcafe.model.cart.Cart;
import com.ifcodedeveloper.cakwangcafe.model.cart.GetCart;
import com.ifcodedeveloper.cakwangcafe.model.customer.Customer;
import com.ifcodedeveloper.cakwangcafe.model.transaction.PostTransaction;
import com.ifcodedeveloper.cakwangcafe.model.transaction.TotalHarga;
import com.ifcodedeveloper.cakwangcafe.rest.ApiClient;
import com.ifcodedeveloper.cakwangcafe.rest.ApiInterface;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnCheckout;
    TextView tv_total;
    ApiInterface mApiInterface;
    private RecyclerView mRecyclerView;
    private CartAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    Context mContext;
    //    private CartAdapter cAdapter;
    ArrayList<Cart> cartList = new ArrayList<>();
    Customer customer = new Customer();
    public static final String EXTRA_CUSTOMER = "extra_customer";
    SharedPreferences sharedPreferences;
    String nama_pelanggan, no_meja, sub, total, shift,date,time;
    Integer total_harga;
    CartAdapter sAdapter;
    public static boolean add = true;
//    int sum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        tv_total = findViewById(R.id.tv_total_harga);
        btnCheckout = findViewById(R.id.btn_checkout);
        btnCheckout.setOnClickListener(this);

        mRecyclerView = findViewById(R.id.rv_cart);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mApiInterface = ApiClient.getClient().create(ApiInterface.class);

        sharedPreferences = getSharedPreferences("pelanggan", Context.MODE_PRIVATE);
        nama_pelanggan = sharedPreferences.getString("nama_pelanggan", "0");
        no_meja = sharedPreferences.getString("no_meja", "0");
//        customer = getIntent().getParcelableExtra(EXTRA_CUSTOMER);

        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
//                Customer customer = new Customer();
//                customer.setNama_pelanggan(customer.getNama_pelanggan());
//                customer.setNo_meja(customer.getNo_meja());
                Intent intent = new Intent(CartActivity.this, DeleteCartActivity.class);
//                intent.putExtra(EXTRA_CUSTOMER,customer);
                startActivity(intent);
            }
        });
//        int totalPrice = 0;
//        for (int i = 0; i<cartList.size(); i++)
//        {
//            totalPrice += cartList.get(i).getSubtotal();
//        }
//        int subtotal = mAdapter.grandTotal();

        sAdapter = new CartAdapter(cartList, mContext);
        tv_total.setText(String.valueOf(sAdapter.grandTotal()));
//tv_total.setText(String.valueOf(grandTotal()));
        ShowCart();

        TotalHarga();

//        if(checkTime("06:00-19:00")){
//            inRange = true;
//        }
    }

    void Shift(){
        if(checkTime("06:00-19:00")){
            boolean inRange = true;
        }
    }
//    void Check() {
//        if (cartList == null){
//            Log.e("kosong", "Data Kososng");
//        }else {
//            Log.e("Valid", "Data tidak Kosong");
//            TotalHarga();
//        }
//    }

    public void ShowCart() {
        Call<GetCart> ItemCall = mApiInterface.getCart(nama_pelanggan, no_meja);
        ItemCall.enqueue(new Callback<GetCart>() {
            @Override
            public void onResponse(Call<GetCart> call, Response<GetCart>
                    response) {
                cartList = response.body().getListDataCart();

                Log.d("Retrofit Get", "Jumlah data Item: " + String.valueOf(cartList.size()));
                mAdapter = new CartAdapter(cartList, mContext);
                mRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onFailure(Call<GetCart> call, Throwable t) {
                Log.e("Retrofit Get", t.toString());
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_checkout:
                if (tv_total.getText() != "Keranjang Kosong") {
                    Intent checkout = new Intent(CartActivity.this, TransactionActivity.class);
                    PostTrans();
                    startActivity(checkout);
                } else {
                    Toast.makeText(CartActivity.this, "Keranjang Tidak Boleh Kosong", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    void TotalHarga() {
        final Call<TotalHarga> getTotal = mApiInterface.getTotal(nama_pelanggan, no_meja);
        getTotal.enqueue(new Callback<TotalHarga>() {
            @Override
            public void onResponse(Call<TotalHarga> call, Response<TotalHarga> response) {
                total_harga = response.body().getTotal_harga();

                Log.e("total", "onResponse: " + total_harga);
                if (total_harga == null) {
                    tv_total.setText("Keranjang Kosong");
                } else {
                    Locale localeID = new Locale("in", "ID");
                    NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
                    tv_total.setText(formatRupiah.format(total_harga));
                }
            }

            @Override
            public void onFailure(Call<TotalHarga> call, Throwable t) {
                Log.e("gagal", "gagal" + t);
            }
        });
    }

    void PostTrans() {
        date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        time = new SimpleDateFormat("kk:mm:ss", Locale.getDefault()).format(new Date());


        Call<PostTransaction> postTrans = mApiInterface.postTrans(nama_pelanggan, no_meja, time, date, total_harga.toString(), "A");
        postTrans.enqueue(new Callback<PostTransaction>() {
            @Override
            public void onResponse(Call<PostTransaction> call, Response<PostTransaction> response) {
                Toast.makeText(getApplicationContext(), "Berhasil ditambahkan", Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onFailure(Call<PostTransaction> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
            }
        });
    }
//    public int grandTotal() {
//        int totalPrice = 0;
//        for (int i = 0; i < cartList.size(); i++) {
////            String sub = cartList.get(i).getSub_total();
//            int tHarga = Integer.parseInt(total);
//            totalPrice += tHarga;
//        }
//
//        return totalPrice;
//    }
private Calendar fromTime;
    private Calendar toTime;
    private Calendar currentTime;

    public boolean checkTime(String time) {
        try {
            String[] times = time.split("-");
            String[] from = times[0].split(":");
            String[] until = times[1].split(":");

            fromTime = Calendar.getInstance();
            fromTime.set(Calendar.HOUR_OF_DAY, Integer.valueOf(from[0]));
            fromTime.set(Calendar.MINUTE, Integer.valueOf(from[1]));

            toTime = Calendar.getInstance();
            toTime.set(Calendar.HOUR_OF_DAY, Integer.valueOf(until[0]));
            toTime.set(Calendar.MINUTE, Integer.valueOf(until[1]));

            currentTime = Calendar.getInstance();
            currentTime.set(Calendar.HOUR_OF_DAY, Calendar.HOUR_OF_DAY);
            currentTime.set(Calendar.MINUTE, Calendar.MINUTE);
            if(currentTime.after(fromTime) && currentTime.before(toTime)){
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

}
