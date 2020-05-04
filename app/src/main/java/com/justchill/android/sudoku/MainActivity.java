package com.justchill.android.sudoku;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {


    Game game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        game = new Game(9);

        game.setBoardView((BoardView) findViewById(R.id.board_view));
        game.start();


        //the layout on which you are working
        LinearLayout layout = findViewById(R.id.number_buttons_parent_layout);

        for(int i = 1; i <= 9; i++) {
            //set the properties for button
            Button button = new Button(this);
            button.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            button.setText(String.valueOf(i));
            button.setId(1000 + i);

            final int number = i;

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    game.onNumberClick(number);
                }
            });

            //add button to the layout
            layout.addView(button);
        }

        final Button notesSwitch = findViewById(R.id.btn_notes_switch);
        notesSwitch.setText("Off");

        notesSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notesSwitch.setText(game.onNotesSwitchClick() ? "Off" : "On");
            }
        });


    }

}
