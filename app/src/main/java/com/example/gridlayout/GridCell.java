package com.example.gridlayout;

import android.widget.TextView;

public class GridCell {

    TextView tv;
    boolean Bomb = false;
    boolean flagged = false;
    boolean revealed = false;
    int bombsInArea = 0;
    int row = -1;
    int col = -1;

    public GridCell(TextView tv, int row, int col){
        this.tv = tv;
        this.row = row;
        this.col = col;
    }

    public TextView getTv() {
        return tv;
    }

    public int getRow() {
        return row;
    }
    public int getCol(){
        return col;
    }
    public boolean isRevealed() {
        return revealed;
    }
    public void setRevealed(boolean revealed){
        this.revealed = revealed;
    }
    public boolean isBomb() {
        return Bomb;
    }
    public void setBomb(boolean bomb) {
        this.Bomb = bomb;
    }

    public boolean isFlagged() {
        return flagged;
    }
    public void setFlagged(boolean flagged){
        this.flagged = flagged;
    }

    public int getBombsInArea() {
        return bombsInArea;
    }
    public void increaseBombsInArea(){
        bombsInArea++;
    }


}
