package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.Queue;
import java.util.Random;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int COLUMN_COUNT = 8;

    // save the TextViews of all cells in an array, so later on,
    // when a TextView is clicked, we know which cell it is
    private ArrayList<GridCell> cells;

    private ArrayList< ArrayList<Integer> > indexByLocation;
    private boolean gameStarted = false;
    private int clock = 0;
    private boolean running = false;
    private boolean flagTool = false;

    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            clock = savedInstanceState.getInt("clock");
            running = savedInstanceState.getBoolean("running");
        }

        runTimer();

        cells = new ArrayList<GridCell>();
        indexByLocation = new ArrayList< ArrayList<Integer> >();

        // add dynamically created cells
        GridLayout grid = (GridLayout) findViewById(R.id.gridLayout01);

        int counter = 0;
        for (int i = 0; i<=9; i++) {
            ArrayList<Integer> row = new ArrayList<>();
            for (int j=0; j<=7; j++) {
                TextView tv = new TextView(this);
                tv.setHeight( dpToPixel(32) );
                tv.setWidth( dpToPixel(32) );
                tv.setTextSize( 18 );//dpToPixel(32) );
                tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                tv.setTextColor(Color.GREEN);
                tv.setBackgroundColor(Color.GREEN);
                tv.setOnClickListener(this::onClickTV);

                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.setMargins(dpToPixel(2), dpToPixel(2), dpToPixel(2), dpToPixel(2));
                lp.rowSpec = GridLayout.spec(i);
                lp.columnSpec = GridLayout.spec(j);

                grid.addView(tv, lp);
                GridCell gc = new GridCell(tv, i, j);
                row.add(counter);
                counter++;
                cells.add(gc);

            }
            indexByLocation.add(row);
        }

    }

    private void toggleFlagTool(){
        // add some code to change the flag at the bottom from a pickaxe to a mine
        //also run this function as an onclick Listener for that bottom flag textView
        if(flagTool){
            flagTool = false;
        }
        else{
            flagTool = true;
        }

    }

    private int findIndexOfCellTextView(TextView tv) {
        for (int n=0; n<cells.size(); n++) {
            if (cells.get(n).getTv() == tv)
                return n;
        }
        return -1;
    }



    private GridCell getGridCellFromTextView(TextView tv){
        for(GridCell gc: cells){
            if(gc.getTv()==tv){
                return gc;
            }
        }
        return null;
    }

    //intialize the four bombs
    private void initalizeBombs(TextView view){
        int max = cells.size() -1;
        int min = 0;

        //intialize 4 random cells for bombs and ensure they were not the initial one that was clicked
        int random_int1 = (int)Math.floor(Math.random()*(max-min+1)+min);
        while(random_int1==findIndexOfCellTextView(view)){
            random_int1 = (int)Math.floor(Math.random()*(max-min+1)+min);
        }
        int random_int2 = (int)Math.floor(Math.random()*(max-min+1)+min);
        while(random_int2==findIndexOfCellTextView(view)){
            random_int2 = (int)Math.floor(Math.random()*(max-min+1)+min);
        }
        int random_int3 = (int)Math.floor(Math.random()*(max-min+1)+min);
        while(random_int3==findIndexOfCellTextView(view)){
            random_int3 = (int)Math.floor(Math.random()*(max-min+1)+min);
        }
        int random_int4 = (int)Math.floor(Math.random()*(max-min+1)+min);
        while(random_int4==findIndexOfCellTextView(view)){
            random_int4 = (int)Math.floor(Math.random()*(max-min+1)+min);
        }

        cells.get(random_int1).setBomb(true);
        cells.get(random_int2).setBomb(true);
        cells.get(random_int3).setBomb(true);
        cells.get(random_int4).setBomb(true);

        addNumbersToCells();

        return;

    }

    private void reveal(GridCell gc){

        if(gc.isRevealed()){
            return;
        }

        TextView tv = gc.getTv();
        gc.setRevealed(true);

        if(gc.isBomb()){
            tv.setText("@string/mine");
            tv.setBackgroundColor(Color.RED);
            //end the game
            EndGame();
            return;
        }

        if(gc.getBombsInArea()>0){
            tv.setText(String.valueOf(gc.getBombsInArea()));
            tv.setTextColor(Color.GRAY);
            tv.setBackgroundColor(Color.LTGRAY);
        }
        else if(gc.getBombsInArea()==0){
            tv.setTextColor(Color.GRAY);
            tv.setBackgroundColor(Color.LTGRAY);
        }

    }

    //BFS out from the current view and open all views that don't have a number as well as ones on the edges
    private void BFS(GridCell gc){

        GridCell start = gc;
        ArrayList<GridCell> queue = new ArrayList<>();

        queue.add(start);
        reveal(start);

        while(queue.size()>0){

            //grab from the end of the queue
            GridCell v = queue.get(queue.size()-1);
            //remove last thing in the queue
            queue.remove(queue.size()-1);

            //for loop through all of the neighbors
            for(int dr=-1; dr<=1; dr++){
                for(int dc=-1; dc<=1; dc++){

                    int r = v.getRow()+dr;
                    int c = v.getCol()+dc;

                    //if the neighbor is inbounds then check it
                    if(r>=0 && r<=9 && c>=0 && c<=7){
                        //create neighbor
                        GridCell neighbor = cells.get(indexByLocation.get(r).get(c));

                        //if neighbor is a bomb or if its already revealed skip it
                        if(neighbor.isBomb() || neighbor.isRevealed()){
                            continue;
                        }
                        //if neighbor has a number associated with it just reveal it and more on
                        else if (neighbor.getBombsInArea()>0){
                            reveal(neighbor);
                        }

                        //if the neighbor is a blank square then add it to queue and BFS that ones neighbors
                        else if(neighbor.getBombsInArea()==0){
                            reveal(neighbor);
                            queue.add(neighbor);
                        }
                    }

                }
            }


        }






    }

    //add the correct numbers to all the cells around a bomb
    private void addNumbersToCells(){
        for(GridCell gc: cells){
            //get cells around it by location
            for(int dr=-1; dr<=1; dr++){
                for(int dc=-1; dc<=1; dc++){
                    int r = gc.getRow()+dr;
                    int c = gc.getCol()+dc;

                    if(r>=0 && r<=9 && c>=0 && c<=7){
                        //if the cell that corresponds to the location we are checking is a bomb
                        //increase bombs in the area for the current cell we are evaluating
                        if(cells.get(indexByLocation.get(r).get(c)).isBomb()){
                            gc.increaseBombsInArea();
                        }
                    }

                }
            }

        }

    }

    private void EndGame(){
        onClickStop();
        for(GridCell gc: cells){
            reveal(gc);
        }
    }



    public void onClickTV(View view){
        TextView tv = (TextView) view;
        GridCell gc = getGridCellFromTextView(tv);

        //if the game has not started yet, start the game
        if(!gameStarted) {
            //run function to intialize the bombs
            initalizeBombs(tv);
            onClickStart();
            gameStarted = true;
        }
        //show bomb and end the game
        if(gc.isBomb()){
            tv.setText("@string/mine");
            tv.setBackgroundColor(Color.RED);
            //run function to end the game . . .
            EndGame();
            return;
        }
        // its a flagged cell, unflag it
        //this might have to be a separate if statement later depending on if
        //the flag tool is in use or not
        else if(gc.isFlagged()){
            return;
        }
        //its a number block, in which case just reveal it
        else if(gc.getBombsInArea()>0){
            reveal(gc);
        }

        //its an empty block, run BFS
        else if(gc.getBombsInArea()==0){
            //run BFS
            BFS(gc);
        }
        //make the revealed boolean true
        gc.setRevealed(true);

    }


    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("clock", clock);
        savedInstanceState.putBoolean("running", running);
    }

    public void onClickStart() {
        running = true;
    }

    public void onClickStop() {
        running = false;
    }
    public void onClickClear(View view) {
        running = false;
        clock = 0;
    }

    private void runTimer() {
        final TextView timeView = (TextView) findViewById(R.id.textView);
        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
//                int hours =clock/3600;
//                int minutes = (clock%3600) / 60;
                int seconds = clock;
                String time = String.format("%02d", seconds);
                timeView.setText(time);

                if (running) {
                    clock++;
                }
                handler.postDelayed(this, 1000);
            }
        });
    }









}


//        int n = findIndexOfCellTextView(tv);
//        int i = n/COLUMN_COUNT;
//       // int j = n%COLUMN_COUNT;
//        tv.setText(String.valueOf(i));
//        if (tv.getCurrentTextColor() == Color.GRAY) {
//            tv.setTextColor(Color.GREEN);
//            tv.setBackgroundColor(Color.parseColor("lime"));
//        }else {
//            tv.setTextColor(Color.GRAY);
//            tv.setBackgroundColor(Color.LTGRAY);
//        }