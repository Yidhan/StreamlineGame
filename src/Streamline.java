

import java.util.*;
import java.io.*;

/** This class defines how the player controls the pieces of the
  * Streamline game and how the game is updated
  * when the player makes moves.
  * */
public class Streamline {

    final static int DEFAULT_HEIGHT = 6;
    final static int DEFAULT_WIDTH = 5;
    final static String w = "w";
    final static String a = "a";
    final static String s = "s";
    final static String d = "d";
    final static String u = "u";
    final static String o = "o";
    final static String q = "q";


    final static String OUTFILE_NAME = "saved_streamline_game";

    GameState currentState;
    List<GameState> previousStates;

    /** This is the no-argument constructor of Streamline
      * */
    public Streamline() {
        //Initialize currentState with a default height of 6,
        // a default width of 5,
        // a current player position at the lower left corner of the board,
        // and a goal position of the top right corner of the board.
        this.currentState = new GameState(DEFAULT_HEIGHT,DEFAULT_WIDTH,
                DEFAULT_HEIGHT-1,0,0,DEFAULT_WIDTH-1);
        //Add 3 random obstacles to the current state
        this.currentState.addRandomObstacles(3);
        //Initialize previousStates to an empty ArrayList
        this.previousStates = new ArrayList<>();

    }

    /** This is the constructor that takes
      * a filename and load it to the game
      * @param filename the name of file to load
      * */
    public Streamline(String filename) {
        try {
            loadFromFile(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Take in the parameter filename, read the file content,
      * and initialize the appropriate instance variables
      * @param filename the name of file to load
      * @throws IOException
      * */
    protected void loadFromFile(String filename) throws IOException {

        File file = new File(filename);
        Scanner scan = new Scanner(file);
        //read in the content of file and
        // initialize the respective instance variables
        int height = scan.nextInt();
        int width = scan.nextInt();
        int playerRow = scan.nextInt();
        int playerCol = scan.nextInt();
        int goalRow = scan.nextInt();
        int goalCol = scan.nextInt();
        scan.nextLine();
        currentState =
                new GameState(height,width,playerRow,playerCol,goalRow,goalCol);

        for(int i = 0;  i < height; i++){
            String line = scan.nextLine();
            for(int j = 0; j< width; j++){
                currentState.board[i][j] = line.charAt(j);
            }
        }
        previousStates = new ArrayList<>();

    }

    /** This method first updates previousStates by saving a copy of the
      * current state into the list, and then makes a move in the given
      * direction on currentState.If direction is null, do nothing.
      * @param direction the direction to move
      * */
    void recordAndMove(Direction direction) {
        //if input argument is null, do nothing
        if(direction == null){
            return;
        }
        GameState currentStateCp = new GameState(currentState);
        //move towards the direction
        currentState.move(direction);
        //when the previousStates has at least one element,
        //check if the currentState is equal to the GameState before move
        //if yes, return, do not update previousStates
        if(previousStates.size()>0 &&
                currentState.equals(currentStateCp))
        {
            return;
        }
        //if no, add the GameState before move to previousStates
        previousStates.add(currentStateCp);


    }

    /** Undo the most recent move made by the player
      * */
    void undo() {
        //when the previousStates has at least one element
        if(previousStates.size()>0)
        {
            //update the previousStates, currentState by removing
            //the last element on the list, and assign it to currentState
            currentState = previousStates.get(previousStates.size() - 1);
            previousStates.remove(previousStates.size() - 1);
        }
    }

    /** This method enables the player to control the pieces of the
      * Streamline game and the game is updated when the player makes moves.
      * */
    void play() {

        while(true)
        {
            boolean quit;
            //check if the game is passed
            if(currentState.levelPassed)
            {
                System.out.println(currentState.toString());
                System.out.println("Level Passed!");
                break;
            }
            //print the currentState of game and prompt
            // the user to input next move
            System.out.println(currentState.toString());
            System.out.print("> ");
            Scanner scan = new Scanner(System.in);
            String str = scan.next();

            //read in the input and check which of the action case it is
            //make the move accordingly
            switch(str)
            {
                case w:
                    recordAndMove(Direction.UP);
                    break;
                case a:
                    recordAndMove(Direction.LEFT);
                    break;
                case s:
                    recordAndMove(Direction.DOWN);
                    break;
                case d:
                    recordAndMove(Direction.RIGHT);
                    break;
                case u:
                    undo();
                    break;
                case o:
                    saveToFile();
                    break;
                case q:
                    quit = true;
                    return;
            }

        }

    }

    /** This method writes the Streamline game
      * to a file in a certain format
      * */
    void saveToFile() {
        try {
            //write the game statics to a file in a certain format
            PrintWriter output = new PrintWriter(new File(OUTFILE_NAME));
            output.print(currentState.board.length);
            output.print(" ");
            output.println(currentState.board[0].length);
            output.print(currentState.playerRow);
            output.print(" ");
            output.println(currentState.playerCol);
            output.print(currentState.goalRow);
            output.print(" ");
            output.println(currentState.goalCol);
            for(int i = 0; i < currentState.board.length;i++) {
                output.println(currentState.board[i]);
            }
            //close the file
            output.close();
            System.out.println("Saved current state to: " +
                    "saved_streamline_game");
            
        } catch(IOException e)
        {
            e.printStackTrace();
        }
    }


}
