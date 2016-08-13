package com.example.chente.recipio;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

public class recipeActivity extends AppCompatActivity {

    private String TAG = "tag";
    private int selectedStep = 0;
    private TextToSpeech tts;
    private String[] recipeSteps;
    private SpeechRecognizer speechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        // init texttospeech
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                tts.setOnUtteranceProgressListener(new ttsUtteranceListener());
            }
        });

        // init speechrecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        // get info about this recipe
        Intent intent = getIntent();
        String recipe = intent.getStringExtra("recipe");
        String id = intent.getStringExtra("id");

        setTitle(recipe);

        final String recipeUrl = "https://recipio.herokuapp.com/recipes?recipe=" + id;
        Ion.with(this)
                .load(recipeUrl)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        if (e != null) { // error occurred
                            Toast.makeText(recipeActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                        }
                        
                        JsonArray recipeStepsJson = result.getAsJsonArray("steps");
                        int length = recipeStepsJson.size();
                        recipeSteps = new String[length];
                        for (int i = 0; i < length; i++) {
                            recipeSteps[i] = recipeStepsJson.get(i).toString();
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                recipeActivity.this,
                                android.R.layout.simple_list_item_1,
                                android.R.id.text1,
                                recipeSteps
                        );

                        ListView stepsList = (ListView) findViewById(R.id.stepsList);
                        stepsList.setAdapter(adapter);

                        speak();
                    }
                });
    }

    private void speak() {
        tts.speak(recipeSteps[selectedStep], TextToSpeech.QUEUE_FLUSH, null, "TODO??");
        selectedStep++;
    }

    class Listener implements RecognitionListener {
        public void onReadyForSpeech(Bundle params)
        {
            Log.d(TAG, "onReadyForSpeech");
        }
        public void onBeginningOfSpeech()
        {
            Log.d(TAG, "onBeginningOfSpeech");
        }
        public void onRmsChanged(float rmsdB)
        {
            Log.d(TAG, "onRmsChanged");
        }
        public void onBufferReceived(byte[] buffer)
        {
            Log.d(TAG, "onBufferReceived");
        }
        public void onEndOfSpeech()
        {
            Log.d(TAG, "onEndofSpeech");
        }
        public void onError(int error)
        {
            Log.d(TAG,  "error " +  error);
        }
        public void onResults(Bundle results)
        {
            String str = new String();
            Log.d(TAG, "onResults " + results);
            ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < data.size(); i++)
            {
                Log.d(TAG, "result " + data.get(i));
                str += data.get(i);
            }
            Log.d("ay", str);
        }
        public void onPartialResults(Bundle partialResults)
        {
            Log.d(TAG, "onPartialResults");
        }
        public void onEvent(int eventType, Bundle params)
        {
            Log.d(TAG, "onEvent " + eventType);
        }
    }

    class ttsUtteranceListener extends UtteranceProgressListener {
        @Override
        public void onDone(String utteranceId) {
            // Done speaking, so call speech recognizer
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            //... put other settings in the Intent
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 60000);
            startActivityForResult(intent, 123);

        }

        @Override
        public void onError(String utteranceId) {
            Log.d("hello", "error");
        }

        @Override
        public void onStart(String utteranceId) {
        }
    }
}


