package com.example.gridlayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import java.util.Random;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int COLUMN_COUNT = 8;

    // save the TextViews of all cells in an array, so later on,
    // when a TextView is clicked, we know which cell it is
    private ArrayList<GridCell> cells;

    private ArrayList< ArrayList<GridCell> > cellsByLoc;
    private boolean gameStarted = true;

    private int dpToPixel(int dp) {
        float density = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cells = new ArrayList<GridCell>();
        cellsByLoc = new ArrayList<ArrayList<GridCell> >();

        // add dynamically created cells
        GridLayout grid = (GridLayout) findViewById(R.id.gridLayout01);
        for (int i = 0; i<=9; i++) {
            ArrayList<GridCell> row = new ArrayList<>();
            for (int j=0; j<=7; j++) {
                TextView tv = new TextView(this);
                tv.setHeight( dpToPixel(32) );
                tv.setWidth( dpToPixel(32) );
                tv.setTextSize( 12 );//dpToPixel(32) );
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
                row.add(gc);
                cells.add(gc);
            }
            cellsByLoc.add(row);
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

    //BFS out from the current view and open all views that don't have a number as well as ones on the edges
    private void BFS(TextView view){






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
                        if(cellsByLoc.get(r).get(c).isBomb()){
                            gc.increaseBombsInArea();
                        }
                    }

                }
            }

        }

    }



    public void onClickTV(View view){
        TextView tv = (TextView) view;

        //if the game has not started yet, start the game
        if(gameStarted) {
            //run function to intialize the bombs
            initalizeBombs(tv);

            gameStarted = false;
            return;
        }
        //show bomb and end the game
        else if(getGridCellFromTextView(tv).isBomb()){
            tv.setTextColor(Color.RED);
            tv.setBackgroundColor(Color.RED);
            //run function to end the game . . .



            //. . .
            return;
        }
        // its a flagged cell, unflag it
        else if(getGridCellFromTextView(tv).isFlagged()){

            return;
        }
        //its a number block, in which case just reveal it
        else if(getGridCellFromTextView(tv).getBombsInArea()>0){
            tv.setTextColor(Color.GRAY);
            tv.setBackgroundColor(Color.LTGRAY);
        }
        //its an empty block, run BFS
        else if(getGridCellFromTextView(tv).getBombsInArea()==0){

            //for testing
            //tv.setText(String.valueOf(getGridCellFromTextView(tv).getRow())+String.valueOf(getGridCellFromTextView(tv).getCol()));
            tv.setText(String.valueOf(getGridCellFromTextView(tv).getBombsInArea()));

            //run BFS


            tv.setTextColor(Color.GRAY);
            tv.setBackgroundColor(Color.LTGRAY);

        }

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