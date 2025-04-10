package com.example.healthcare;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.Manifest;
import android.os.Bundle;
import android.widget.*;
import android.content.Intent;
import android.net.Uri;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String API_KEY = "REPLACE WITH YOU API KEY"; // Replace here
    private static final String MODEL = "openai/gpt-3.5-turbo";
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    EditText inputText;
    TextView responseText;
    Button sendButton, btnCallAmbulance, btnEmergencyCall, btnEmergencyContact, btnMedReminder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputText = findViewById(R.id.inputText);
        responseText = findViewById(R.id.responseText);
        sendButton = findViewById(R.id.sendButton);
        btnCallAmbulance = findViewById(R.id.btnCallAmbulance);
        btnEmergencyCall = findViewById(R.id.btnEmergencyCall);
        btnEmergencyContact = findViewById(R.id.btnEmergencyContact);
        btnMedReminder = findViewById(R.id.btnMedicationReminder);

        sendButton.setOnClickListener(v -> askAI(inputText.getText().toString()));

        btnCallAmbulance.setOnClickListener(v -> makePhoneCall("108"));
        btnEmergencyCall.setOnClickListener(v -> makePhoneCall("112"));
        btnEmergencyContact.setOnClickListener(v -> makePhoneCall("9876543210")); // Replace with your contact
        Button btnTriage = findViewById(R.id.btnTriage);
        btnTriage.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TriageActivity.class);
            startActivity(intent);
        });


        btnMedReminder.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReminderActivity.class);
            startActivity(intent);
        });
    }


    private void makePhoneCall(String number) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
            return;
        }
        startActivity(callIntent);
    }

    private void askAI(String prompt) {
        OkHttpClient client = new OkHttpClient();

        try {
            JSONObject message = new JSONObject();
            message.put("role", "user");
            message.put("content", prompt);

            JSONArray messages = new JSONArray();
            messages.put(message);

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", MODEL);
            requestBody.put("messages", messages);

            RequestBody body = RequestBody.create(
                    requestBody.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("HTTP-Referer", "https://your-app.com/")
                    .header("X-Title", "HealthCareApp")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> responseText.setText("Error: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String jsonData = response.body().string();
                    try {
                        JSONObject json = new JSONObject(jsonData);
                        JSONArray choices = json.getJSONArray("choices");
                        String reply = choices.getJSONObject(0).getJSONObject("message").getString("content");

                        runOnUiThread(() -> responseText.setText(reply));
                    } catch (Exception e) {
                        runOnUiThread(() -> responseText.setText("Error parsing response"));
                    }
                }
            });
        } catch (Exception e) {
            responseText.setText("Error: " + e.getMessage());
        }
    }
}
