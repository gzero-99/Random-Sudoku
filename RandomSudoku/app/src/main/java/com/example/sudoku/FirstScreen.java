package com.example.sudoku;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class FirstScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_screen);
        Button startBtn = (Button)findViewById(R.id.startBtn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectMenu();
            }
        });
    }
    void selectMenu(){
        String[] difficultyMenu = {"매우 쉬움","쉬움","보통","어려움"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("난이도 선택")
                .setItems(difficultyMenu,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int randN = (int) (Math.random() * 3);
                        switch (which) {
                            case 0:
                                MainActivity.difficulty = MainActivity.VERYEASY[randN];
                                break;
                            case 1:
                                MainActivity.difficulty = MainActivity.EASY[randN];
                                break;
                            case 2:
                                MainActivity.difficulty = MainActivity.NORMAL[randN];
                                break;
                            case 3:
                                MainActivity.difficulty = MainActivity.HARD[randN];
                                break;
                        }
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}