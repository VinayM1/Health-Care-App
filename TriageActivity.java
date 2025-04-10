package com.example.healthcare;

import com.airbnb.lottie.LottieAnimationView;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class TriageActivity extends AppCompatActivity {

    EditText symptomInput;
    Button assessButton;
    TextView resultText;
    LottieAnimationView animView;

    // Replace with your actual OpenRouter API Key
    String API_KEY = "Bearer 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";
    String BASE_URL = "https://openrouter.ai/api/v1/chat/completions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triage);

        symptomInput = findViewById(R.id.symptomInput);
        assessButton = findViewById(R.id.assessButton);
        resultText = findViewById(R.id.resultText);
        animView = findViewById(R.id.triageAnim);
        animView.setVisibility(View.GONE); // Hide animation initially

        assessButton.setOnClickListener(v -> {
            String input = symptomInput.getText().toString().trim();
            if (!input.isEmpty()) {
                sendToGPT(input);
            } else {
                Toast.makeText(this, "Please enter symptoms", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendToGPT(String symptoms) {
        OkHttpClient client = new OkHttpClient();

        JSONObject json = new JSONObject();
        try {
            json.put("model", "openai/gpt-3.5-turbo");

            JSONArray messages = new JSONArray();

            // System message: instruct AI to only give triage level and recommendation
            messages.put(new JSONObject()
                    .put("role", "system")
                    .put("content", "You are a medical triage assistant. When the user describes symptoms, analyze their severity and reply ONLY in this format:\n\nTriage Level: [Low/Moderate/High/Critical]\nRecommendation: [short advice or what to do next]"));

            // User message
            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", symptoms));

            json.put("messages", messages);

        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(BASE_URL)
                .addHeader("Authorization", API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> resultText.setText("Network error: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String resStr = response.body().string();
                        JSONObject resJson = new JSONObject(resStr);
                        String reply = resJson
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");

                        runOnUiThread(() -> {
                            resultText.setText(reply);

                            // 🔥 Show animation
                            animView.setVisibility(View.VISIBLE);
                            animView.playAnimation();

                            // Hide after 5 seconds
                            new Handler().postDelayed(() -> {
                                animView.cancelAnimation();
                                animView.setVisibility(View.GONE);
                            }, 500000);
                        });

                    } catch (Exception e) {
                        runOnUiThread(() -> resultText.setText("Parsing error."));
                    }
                } else {
                    runOnUiThread(() -> resultText.setText("API Error: " + response.code()));
                }
            }
        });
    }
}
