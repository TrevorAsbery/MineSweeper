package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.Intent;
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
    private int flagCount = 4;
    private boolean won = false;
    private boolean lost = false;

    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //start the timer thread, it will wait for a click to start the clock
        runTimer();

        //this is the toggle view, you want to set its on click listener to the toggle flag function
        TextView flagView = (TextView) findViewById(R.id.Flag);
        flagView.setOnClickListener(this::toggleFlagTool);

        //this view changes with the flag counts, set the intial value
        TextView flagCountView = (TextView) findViewById(R.id.flagCount);
        flagCountView.setText(String.valueOf(flagCount));

        //I use both an array list to store the GridCells
        //and a 2D arraylist to store their indicies in the cells array list
        cells = new ArrayList<GridCell>();
        indexByLocation = new ArrayList< ArrayList<Integer> >();

        // add dynamically created cells
        GridLayout grid = (GridLayout) findViewById(R.id.gridLayout01);

        //count how many cells we have gone through so far
        int counter = 0;

        //for loop through and initialize all cells
        for (int i = 0; i<=9; i++) {
            ArrayList<Integer> row = new ArrayList<>();
            for (int j=0; j<=7; j++) {

                //set up each text view
                TextView tv = new TextView(this);
                tv.setHeight( dpToPixel(32) );
                tv.setWidth( dpToPixel(32) );
                tv.setTextSize( 18 );//dpToPixel(32) );
                tv.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
                tv.setTextColor(Color.GREEN);
                tv.setBackgroundColor(Color.GREEN);

                //set their onclick listener to onClickTV
                tv.setOnClickListener(this::onClickTV);

                GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
                lp.setMargins(dpToPixel(2), dpToPixel(2), dpToPixel(2), dpToPixel(2));
                lp.rowSpec = GridLayout.spec(i);
                lp.columnSpec = GridLayout.spec(j);

                //add the view to the grid and create a new grid cell and add that to the arraylists
                grid.addView(tv, lp);
                GridCell gc = new GridCell(tv, i, j);
                row.add(counter);
                counter++;
                cells.add(gc);

            }
            indexByLocation.add(row);
        }

    }

    public void toggleFlagTool(View v){

        //toggle between pick and flagging tool
        TextView tv = (TextView) v;
        if(flagTool){
            tv.setText(R.string.pick);
            flagTool = false;
        }
        else{
            tv.setText(R.string.flag);
            flagTool = true;
        }

    }

    //find the index of the text view in cells
    private int findIndexOfCellTextView(TextView tv) {
        for (int n=0; n<cells.size(); n++) {
            if (cells.get(n).getTv() == tv)
                return n;
        }
        return -1;
    }



    //get the object that corresponds with the text view
    private GridCell getGridCellFromTextView(TextView tv){
        for(GridCell gc: cells){
            if(gc.getTv()==tv){
                return gc;
            }
        }
        return null;
    }

    //intialize the four bombs in random locations
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

        //number all of the cells around the bombs
        addNumbersToCells();

        return;

    }

    //reveal a cell
    private void reveal(GridCell gc){

        //if already revealed return
        if(gc.isRevealed()){
            return;
        }

        TextView tv = gc.getTv();
        gc.setRevealed(true);

        //if its a bomb set it to bomb and end the game
        if(gc.isBomb()){
            tv.setText(R.string.mine);
            tv.setBackgroundColor(Color.RED);
            //end the game
            EndGame();
            return;
        }

        //numbered cell reveal
        if(gc.getBombsInArea()>0){
            tv.setText(String.valueOf(gc.getBombsInArea()));
            tv.setTextColor(Color.GRAY);
            tv.setBackgroundColor(Color.LTGRAY);
        }
        //blank cell reveal
        else if(gc.getBombsInArea()==0){
            tv.setTextColor(Color.GRAY);
            tv.setBackgroundColor(Color.LTGRAY);
        }

    }

    //BFS out from the current view and open all views that don't have a number as well as ones on the edges
    private void BFS(GridCell gc){

        //initialize starting grid cell
        GridCell start = gc;
        //initialize queue
        ArrayList<GridCell> queue = new ArrayList<>();

        //add first cell to queue and reveal it
        queue.add(start);
        reveal(start);

        //BFS algorithm
        while(queue.size()>0){
            //grab from the end of the queue
            GridCell v = queue.get(queue.size()-1);
            //remove last thing in the queue
            queue.remove(queue.size()-1);

            //for loop through all of the neighbors
            for(int dr=-1; dr<=1; dr++){
                for(int dc=-1; dc<=1; dc++){

                    //current row and col
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
                            //if the cell is flagged, unflag it before revealing
                            if(neighbor.isFlagged()) {
                                TextView tv = neighbor.getTv();
                                tv.setText("");
                                tv.setBackgroundColor(Color.GREEN);
                                flagCount++;
                                ((TextView) findViewById(R.id.flagCount)).setText(String.valueOf(flagCount));
                                gc.setFlagged(false);
                            }
                            //reveal numbered cell
                            reveal(neighbor);
                        }

                        //if the neighbor is a blank square then add it to queue and BFS that ones neighbors
                        else if(neighbor.getBombsInArea()==0){
                            //if the cell is flagged, unflag it before revealing
                            if(neighbor.isFlagged()) {
                                TextView tv = neighbor.getTv();
                                tv.setText("");
                                tv.setBackgroundColor(Color.GREEN);
                                flagCount++;
                                ((TextView) findViewById(R.id.flagCount)).setText(String.valueOf(flagCount));
                                gc.setFlagged(false);
                            }
                            //reveal blank cell and add it to the queue
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

    //run if the player hits a bomb
    private void EndGame(){
        onClickStop();
        lost = true;
        won = false;
        for(GridCell gc: cells){
            reveal(gc);
        }
    }

    //to check if the game has been won yet
    private void WinGame(){
        //count up all cells and see if they each satisify the winning conditions
        int counter = 0;
        for(GridCell gc: cells){
            if((gc.isBomb()) || (gc.isRevealed()&&!gc.isBomb())){
                counter++;
            }
        }

        //if all cells are either a bomb or revealed, end the game with a win
        if(counter==cells.size()){
            onClickStop();
            won = true;
            lost = false;
            return;
        }


    }



    public void onClickTV(View view){
        TextView tv = (TextView) view;
        GridCell gc = getGridCellFromTextView(tv);

        String time = String.valueOf(clock);

        //if the game has already been decided as a win or a lose before this click
        //move to the results screen
        if(won){
            String message = "Used " + time + " seconds. \n You Won! \n Great Work!";
            Intent intent = new Intent(this, DisplayMessageActivity.class);
            intent.putExtra("result", message);
            startActivity(intent);
            return;
        }
        else if(lost){
            String message = "Used " + time + " seconds. \n You Lost \n Try Again!";
            Intent intent = new Intent(this, DisplayMessageActivity.class);
            intent.putExtra("result", message);
            startActivity(intent);
            return;
        }

        //if the game has not started yet, start the game
        if(!gameStarted) {
            //run function to intialize the bombs
            initalizeBombs(tv);
            onClickStart();
            gameStarted = true;
        }
        //if the flag tool is on
        if(flagTool) {
            //if a cell is not revealed and is not flagged, flag it
            if (!gc.isRevealed() && !gc.isFlagged()) {
                tv.setText(R.string.flag);
                tv.setBackgroundColor(Color.GREEN);
                flagCount--;
                ((TextView) findViewById(R.id.flagCount)).setText(String.valueOf(flagCount));
                gc.setFlagged(true);
                WinGame();
                return;

            }
            //if a cell is not revealed and is flagged, unflag it
            else if (!gc.isRevealed() && gc.isFlagged()) {
                tv.setText("");
                tv.setBackgroundColor(Color.GREEN);
                flagCount++;
                ((TextView) findViewById(R.id.flagCount)).setText(String.valueOf(flagCount));
                gc.setFlagged(false);
                WinGame();
                return;
            }
        }

        // if its flagged, move on
        if(gc.isFlagged()){
            return;
        }
        //show bomb and end the game
        if(gc.isBomb()){
            tv.setText(R.string.mine);
            tv.setBackgroundColor(Color.RED);
            //run function to end the game . . .
            EndGame();
            return;
        }

        //its a number block, just reveal it
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
        WinGame();

    }


    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt("clock", clock);
        savedInstanceState.putBoolean("running", running);
    }

    //starts the timer
    public void onClickStart() {
        running = true;
    }
    //ends the timer
    public void onClickStop() {
        running = false;
    }
    //clears the timer
    public void onClickClear(View view) {
        running = false;
        clock = 0;
    }

    //runs the timer
    private void runTimer() {
        final TextView timeView = (TextView) findViewById(R.id.textView);
        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                int seconds = clock;
                //display the seconds the game has been running for
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


