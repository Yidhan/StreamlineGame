

import java.util.*;

/** This class defines the grid on which we will play the game
  * and the positions of the pieces on the board.
  * */
public class GameState {

    // Used to populate char[][] board below and to display the
    // current state of play.
    final static char TRAIL_CHAR = '.';
    final static char OBSTACLE_CHAR = 'X';
    final static char SPACE_CHAR = ' ';
    final static char CURRENT_CHAR = 'O';
    final static char GOAL_CHAR = '@';
    final static char NEWLINE_CHAR = '\n';
    final static char TOPBORDER_CHAR = '-';
    final static char SIDEBORDER_CHAR = '|';
    final static int two = 2;
    final static int fourDirections = 4;
    final static int up = 1;
    final static int left = 2;
    final static int down = 3;
    // This represents a 2D map of the board
    char[][] board;

    // Location of the player
    int playerRow;
    int playerCol;

    // Location of the goal
    int goalRow;
    int goalCol;

    // true means the player completed this level
    boolean levelPassed;

    /** Initialize the board with the given parameters
      * and fill it with SPACE_CHARs, and initialize all other
      * instance variables with the given parameters
      * @param height the number of rows of board
      * @param width the number of columns of board
      * @param playerRow the row index of player location
      * @param playerCol the col index of player location
      * @param goalRow the row index of goal location
      * @param goalCol the col index of goal location
      */

    public GameState(int height, int width, int playerRow, int playerCol, 
                     int goalRow, int goalCol) {
        // set corresponding fields to parameters
        this.playerRow = playerRow;
        this.playerCol = playerCol;
        this.goalRow = goalRow;
        this.goalCol = goalCol;
        this.levelPassed = (playerRow == goalRow)
                && (playerCol == goalCol);
        // initialize a board of given parameters
        this.board = new char[height][width];
        //fill the board with SPACE_CHAR
        for (int row = 0; row < height; row++){
            for(int col = 0; col < width; col++){
                board[row][col] = SPACE_CHAR;
            }
        }
    }

    /** The copy constructor of GameState
      * @param other the GameState object we want to copy from
      */
    public GameState(GameState other) {

        this.board = new char[other.board.length][other.board[0].length];
        for (int row = 0; row < other.board.length ; row++){
            for(int col = 0; col < other.board[0].length; col++){
                board[row][col] = other.board[row][col];
            }
        }
        //copy all the instance variables from GameState other
        this.playerRow = other.playerRow;
        this.playerCol = other.playerCol;
        this.goalRow = other.goalRow;
        this.goalCol = other.goalCol;
        this.levelPassed = other.levelPassed;

    }

    /** Add count number of random obstacles into this.board
      * @param count how many obstacles to add
      * */
    void addRandomObstacles(int count) {
        //If count is a larger number than there are empty spaces available
        //or count is less than 0, return immediately
        int counter = 0;
        for(int i = 0; i < board.length; i++){
            for(int j = 0; j< board[0].length; j++){
                if(board[i][j] == SPACE_CHAR);
                counter = counter + 1;
            }
        }
        if(count < 0 || count > (counter-two)){
            return;
        }
        //create variables to generate numbers for obstacle position
        Random row = new Random();
        Random col = new Random();

        //loop to add desired numbers of obstacles
        for(int i = 0; i < count; i++)
        {
            int randomRow = row.nextInt(board.length);
            int randomCol = col.nextInt(board[0].length);

            //if the obstacle position overlaps with player or
            //goal position, do not add the obstacle,
            // and do the loop again
            if(randomRow == playerRow && randomCol == playerCol){
                count = count + 1;
            }else if(randomRow == goalRow && randomCol == goalCol){
                count = count + 1;
            }else if(board[randomRow][randomCol]
                    != SPACE_CHAR) {
                count = count +1;
            }
            else
            {
                board[randomRow][randomCol]
                        = OBSTACLE_CHAR;
            }
        }
    }


    /** Rotate the board clockwise once
      * rotation should account for all instance var including board,
      * current position, goal position
      * */
    void rotateClockwise() {

        //create 2D array rotatedArray
        int row = board[0].length;
        int col = board.length;
        char[][] rotatedArray = new char[row][col];

        //loop through the 2D array and copy
        //elements from the existing board in the correct positions
        for(int i = 0; i < row; i++){
            for(int j = 0; j < col; j++){
                rotatedArray[i][j] = board[col-j-1][i];
            }
        }
        //update the instance variables after the rotate
        this.board = rotatedArray;
        int originalPlayerRow = playerRow;
        this.playerRow = playerCol;
        this.playerCol = col- originalPlayerRow -1;
        int originalGoalRow = goalRow;
        this.goalRow = goalCol;
        this.goalCol = col - originalGoalRow -1;
    }


    /** Move current position towards right until stopped by obstacle / edge
      * leave a trail of dots for all positions that we're walked through
      * before stopping
      * */
    void moveRight() {

        while (true) {
            //check if the player position can move right
            boolean outOfBounds = !(playerCol + 1 < board[0].length);
            if (outOfBounds) {
                return;
            }
            //check if the player will reach the goal with the move
            boolean reachGoal = (playerRow == goalRow)
                    && ((playerCol + 1) == goalCol);
            if (reachGoal) {
                levelPassed = true;
                board[playerRow][playerCol] = TRAIL_CHAR;
                playerCol = playerCol+1;
                return;
            }
            //check if the player reaches an obstacle or a
            // trail char with the move
            boolean reachObs =
                    board[playerRow][playerCol + 1] == OBSTACLE_CHAR;
            boolean reachTrial =
                    board[playerRow][playerCol + 1] == TRAIL_CHAR;
            if (reachObs || reachTrial) {
                return;
            }
            //add a trail char to the move path
            board[playerRow][playerCol] = TRAIL_CHAR;
            //update the player location after the move
            this.playerCol += 1;

        }

    }

    /** Player moves towards the input direction
      * @param direction
      * */
    void move(Direction direction) {
        // if move right, no rotate
        if (direction == Direction.RIGHT){
            moveRight();
        }
        //if move up, rotate once, move and rotate back
        if (direction == Direction.UP){
            rotateClockwise();
            moveRight();
            for(int i = 0; i < fourDirections-up; i++) {
                rotateClockwise();
            }
        }
        //if move left, rotate twice, move and rotate back
        if (direction == Direction.LEFT){
            for(int i = 0; i <left;i++){
                rotateClockwise();
            }
            moveRight();
            for(int i = 0; i < fourDirections-left;i++) {
                rotateClockwise();
            }
        }
        //if move down, rotate three times,move and rotate back
        if (direction == Direction.DOWN){
            for(int i = 0; i < down;i++){
                rotateClockwise();
            }
            moveRight();
            for(int i = 0; i < fourDirections-down;i++) {
                rotateClockwise();
            }
        }
    }


    @Override
    /** Compare two GameState objects, returns true if all fields match
      * @para other
      * @return true if all fields match
      * */
    public boolean equals(Object other) {

        // check if Object other is a instance of GameState
        if(other instanceof GameState) {
            // check if all fields of the two GameState objects match
            if(this.playerRow != ((GameState) other).playerRow){
                return false;
            }
            if(this.playerCol != ((GameState) other).playerCol){
                return false;
            }
            if(this.goalRow != ((GameState)other).goalRow){
                return false;
            }
            if(this.goalCol != ((GameState)other).goalCol){
                return false;
            }
            if(this.levelPassed != ((GameState) other).levelPassed){
                return false;
            }
            //if the board of the two GameState objects
            // are both null,return true
            if(((GameState)other).board == null && this.board == null)
            {
                return true;
            } else if(((GameState)other).board == null || this.board == null){
                return false;
            }
            if(this.board.length !=((GameState)other).board.length){
                return false;
            }
            if(this.board[0].length !=((GameState)other).board[0].length){
                return false;
            }
            for(int i = 0; i<this.board.length;i++){
                for(int j = 0; j< this.board[0].length;j++){
                    if(this.board[i][j] != ((GameState)other).board[i][j]){
                        return false;
                    }
                }
            }
            //exhausted all possibility of mismatch, they're identical
            return true;
        }
        //if other is not a GameState object, return false
        return false;
    }

    /** Return a String representation of the calling GameState object
      * @return String representation of the calling GameState object
      * */
    @Override
    public String toString() {
        int width = this.board[0].length;
        int height = this.board.length;
        int bodyWidth = 2 * width + 3;

        //create border String for the printed board
        char[] border = new char[bodyWidth];
        for(int i = 0; i< bodyWidth;i++) {
            border[i] = TOPBORDER_CHAR;
        }
        StringBuilder borderString = new StringBuilder();
        for(int i = 0; i < border.length;i++ ){
            borderString.append(border[i]);
        }
        borderString.append(NEWLINE_CHAR);

        //create body String for the printed board
        char[][] body = new char[height][bodyWidth];
        for(int i = 0; i < height; i++){
            for(int j = 0; j< bodyWidth; j++){
                if(j==0||j==bodyWidth-1) {
                    body[i][j] = SIDEBORDER_CHAR;
                }
                else if(j%two==1){
                    body[i][j] = SPACE_CHAR;
                }
                else{
                    body[i][j] = this.board[i][(j - two)/two];
                    if(i == goalRow && (j-two)/2 == goalCol){
                        body[i][j] = GOAL_CHAR;
                    }
                    if(i == playerRow && (j-two)/two == playerCol){
                        body[i][j] = CURRENT_CHAR;
                    }
                }
            }
        }

        StringBuilder bodyString = new StringBuilder();
        for(int i = 0; i < height; i++) {
            for (int j = 0; j < bodyWidth; j++) {
                bodyString.append(body[i][j]);
            }
            bodyString.append(NEWLINE_CHAR);
        }
        //put everything together
        StringBuilder s = new StringBuilder();
        s.append(borderString);
        s.append(bodyString);
        s.append(borderString);

        return s.toString();
    }


    /*public static void main(String [] args){
        GameState game1 = new GameState(5,5,0,0,0,4);
        System.out.println(game1.toString());
        game1.move(Direction.DOWN);
        game1.move(Direction.RIGHT);
        game1.move(Direction.UP);
        System.out.println(game1.toString());
        System.out.println(game1.levelPassed);



    }*/




}
