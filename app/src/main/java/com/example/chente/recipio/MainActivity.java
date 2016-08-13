package com.example.chente.recipio;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goToRecipeActivity(View view) {
        Button b = (Button) view;
        Intent intent = new Intent(MainActivity.this, recipeActivity.class);
        intent.putExtra("id", b.getContentDescription());
        intent.putExtra("recipe", b.getText().toString());
        startActivity(intent);
    }
}
