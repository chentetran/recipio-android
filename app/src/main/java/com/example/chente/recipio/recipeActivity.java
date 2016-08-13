package com.example.chente.recipio;

import android.content.Intent;
import android.os.Bundle;
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

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

public class recipeActivity extends AppCompatActivity implements RecognitionListener {

    private static final String KWS_SEARCH = "wakeup";

    private static final String NEXT = "hello friend";
    private static final String BACK = "back";
    private static final String REPEAT = "repeat";

    private String id;
    private String TAG = "tag";
    private int selectedStep = 0;
    private TextToSpeech tts;
    private String[] recipeSteps;
    private SpeechRecognizer recognizer;


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

        // init sphinx voice recognition
//        new AsyncTask<Void, Void, Exception>() {
//            @Override
//            protected Exception doInBackground(Void... voids) {
//                return null;
//            }
//
//
//        }.execute();
                try {
                    Assets assets = new Assets(recipeActivity.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    Log.e("voice_init", e.toString());
                }

        // get info about this recipe
        Intent intent = getIntent();
        String recipe = intent.getStringExtra("recipe");
        id = intent.getStringExtra("id");

        setTitle(recipe);
    }

    @Override
    protected void onResume() {
        super.onResume();

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

                        tts.speak(recipeSteps[selectedStep], TextToSpeech.QUEUE_FLUSH, null, "TODO??");
                    }
                });
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        recognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))

                // Threshold to tune for keyphrase to balance between false alarms and misses
                .setKeywordThreshold(1e-45f)

                // Use context-independent phonetic search, context-dependent is too slow for mobile
                .setBoolean("-allphone_ci", true)

                .getRecognizer();
        recognizer.addListener(this);

        File commandGrammar = new File(assetsDir, "commands.gram");
        recognizer.addKeywordSearch(KWS_SEARCH, commandGrammar);
    }

    private void listen(String searchName) {
        recognizer.stop();

        recognizer.startListening(searchName);

    }
    

    private void speak() {
        recognizer.stop();
        tts.speak(recipeSteps[selectedStep], TextToSpeech.QUEUE_FLUSH, null, "TODO??");
        recognizer.startListening(KWS_SEARCH);
    }

    class ttsUtteranceListener extends UtteranceProgressListener {
        @Override
        public void onDone(String utteranceId) {
            // Done speaking, so call speech recognizer
            listen(KWS_SEARCH);
        }

        @Override
        public void onError(String utteranceId) {
            Log.d("hello", "error");
        }

        @Override
        public void onStart(String utteranceId) {
            recognizer.stop();
        }
    }

    @Override
    public void onEndOfSpeech() {
        Log.d("end", "END OF SPEECH");
        if (!recognizer.getSearchName().equals(KWS_SEARCH))
            listen(KWS_SEARCH);
    }

    @Override
    public void onBeginningOfSpeech() {
        
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis != null) {
            String text = hypothesis.getHypstr();
            text = text.replaceAll("\\s","");


            if (text.equals(NEXT)) {
                recognizer.stop();
            }
            else if (text.equals(BACK)) {
                Toast.makeText(recipeActivity.this, "back", Toast.LENGTH_SHORT).show();
                Log.d("back", String.valueOf(selectedStep));

                selectedStep--;
                tts.speak(recipeSteps[selectedStep], TextToSpeech.QUEUE_FLUSH, null, "TODO??");

            }
            else if (text.equals(REPEAT)) {
                Toast.makeText(recipeActivity.this, "repeat", Toast.LENGTH_SHORT).show();
                Log.d("repeat", String.valueOf(selectedStep));

                tts.speak(recipeSteps[selectedStep], TextToSpeech.QUEUE_FLUSH, null, "TODO??");

            }
        }
    }

    @Override
    public void onError(Exception e) {
        
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        if (text.equals(NEXT)) {
            Toast.makeText(recipeActivity.this, "next", Toast.LENGTH_SHORT).show();
            Log.d("next", String.valueOf(selectedStep));
            selectedStep++;
            tts.speak(recipeSteps[selectedStep], TextToSpeech.QUEUE_FLUSH, null, "TODO??");


        }
        else if (text.equals(BACK)) {
            Toast.makeText(recipeActivity.this, "back", Toast.LENGTH_SHORT).show();
            Log.d("back", String.valueOf(selectedStep));

            selectedStep--;
            tts.speak(recipeSteps[selectedStep], TextToSpeech.QUEUE_FLUSH, null, "TODO??");

        }
        else if (text.equals(REPEAT)) {
            Toast.makeText(recipeActivity.this, "repeat", Toast.LENGTH_SHORT).show();
            Log.d("repeat", String.valueOf(selectedStep));

            tts.speak(recipeSteps[selectedStep], TextToSpeech.QUEUE_FLUSH, null, "TODO??");

        }
    }

    @Override
    public void onTimeout() {
        listen(KWS_SEARCH);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recognizer.cancel();
        recognizer.shutdown();
    }
}


