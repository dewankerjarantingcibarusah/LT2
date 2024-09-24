package com.example.lt2apps;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private EditText input1, input2, input3;
    private Button buttonSubmit, buttonNext, buttonPrev;
    private TextView textViewSheetName;
    private OkHttpClient client = new OkHttpClient(); // HTTP client

    private int currentSheetIndex = 0; // Indeks sheet saat ini
    private List<String> sheets = new ArrayList<>(); // List untuk menyimpan nama sheet

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Menghubungkan EditText dan Button dari layout XML
        input1 = findViewById(R.id.input1);
        input2 = findViewById(R.id.input2);
        input3 = findViewById(R.id.input3);
        buttonSubmit = findViewById(R.id.buttonSubmit);
        buttonNext = findViewById(R.id.buttonNext);
        buttonPrev = findViewById(R.id.buttonPrev);
        textViewSheetName = findViewById(R.id.textViewSheetName);

        // Mendapatkan daftar nama sheet dari Google Sheets
        getSheetsFromSpreadsheet();

        // Listener untuk tombol submit
        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nama = input1.getText().toString();
                String email = input2.getText().toString();
                String umur = input3.getText().toString();

                // Kirim data ke Google Apps Script (Spreadsheet)
                sendDataToSpreadsheet(nama, email, umur);
            }
        });

        // Listener untuk tombol sebelumnya
        buttonPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSheetIndex > 0) {
                    currentSheetIndex--;
                    updateSheetName();
                } else {
                    Toast.makeText(MainActivity.this, "Ini adalah sheet pertama", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Listener untuk tombol berikutnya
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSheetIndex < sheets.size() - 1) {
                    currentSheetIndex++;
                    updateSheetName();
                } else {
                    Toast.makeText(MainActivity.this, "Ini adalah sheet terakhir", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getSheetsFromSpreadsheet() {
        // URL dari Google Apps Script yang mengembalikan daftar sheet
        String url = "https://script.google.com/macros/s/AKfycbzcrt_bGW_peKsuTeZ5ucfou5YKZPoe_QJa2UbuNSAKDtWv8sGZQ9GPTDOhEK4iBwXf/exec";


        // Membangun request untuk GET
        Request request = new Request.Builder()
                .url(url)
                .build();

        // Memanggil request secara asynchronous
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Gagal mendapatkan daftar sheet", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // Parsing daftar sheet dari response
                    String responseBody = response.body().string();
                    // Misalnya, responseBody = "Sheet1,Sheet2,Sheet3"
                    String[] sheetArray = responseBody.split(",");
                    for (String sheet : sheetArray) {
                        sheets.add(sheet.trim());
                    }
                    // Update nama sheet di UI
                    runOnUiThread(() -> {
                        updateSheetName();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Gagal mendapatkan daftar sheet", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void updateSheetName() {
        if (!sheets.isEmpty()) {
            textViewSheetName.setText("Nama Sheet: " + sheets.get(currentSheetIndex));
        }
    }

    private void sendDataToSpreadsheet(String nama, String email, String umur) {
        // URL dari Web Apps GAS
        String url = "https://script.google.com/macros/s/AKfycbzcrt_bGW_peKsuTeZ5ucfou5YKZPoe_QJa2UbuNSAKDtWv8sGZQ9GPTDOhEK4iBwXf/exec";


        // Membangun request body untuk dikirimkan ke server
        FormBody formBody = new FormBody.Builder()
                .add("nama", nama)
                .add("email", email)
                .add("umur", umur)
                .add("sheet", sheets.get(currentSheetIndex)) // Tambahkan informasi sheet
                .build();

        // Membangun request untuk POST
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        // Memanggil request secara asynchronous
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Jika gagal mengirim data
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Gagal mengirim data", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // Jika sukses mengirim data
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Data berhasil dikirim", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
