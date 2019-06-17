

import javafx.scene.*;
import javafx.scene.shape.*;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.animation.*;
import javafx.animation.PathTransition.*;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.*;
import javafx.util.Duration;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * This class defines how the Streamline game
 * is interacted with users.
 * It contains methods to show and play the game.
 */
public class GuiStreamline extends Application {
    static final double SCENE_WIDTH = 500;
    static final double SCENE_HEIGHT = 600;
    static final String TITLE = "CSE 8b Streamline GUI";
    static final String USAGE =
        "Usage: \n" +
        "> java GuiStreamline               - to start a game with default" +
            " size 6*5 and random obstacles\n" +
        "> java GuiStreamline <filename>    - to start a game by reading g" +
            "ame state from the specified file\n" +
        "> java GuiStreamline <directory>   - to start a game by reading a" +
            "ll game states from files in\n" +
        "                                     the specified directory and " +
            "playing them in order\n";

    static final Color TRAIL_COLOR = Color.LIGHTCORAL;
    static final Color GOAL_COLOR = Color.MEDIUMAQUAMARINE;
    static final Color OBSTACLE_COLOR = Color.DIMGRAY;

    //Trail radius will be set to this fraction of the size of a board square.
    static final double TRAIL_RADIUS_FRACTION = 0.1;

    //Squares will be resized to this fraction of the size of a board square.
    static final double SQUARE_FRACTION = 0.8;
    static final int PLAYER_MOVE = 100;

    Scene mainScene;
    Group levelGroup;                   // For obstacles and trails
    Group rootGroup;                    // Parent group for everything else
    Player playerRect;                  // GUI representation of the player
    RoundedSquare goalRect;             // GUI representation of the goal

    Shape[][] grid;                     // Same dimensions as the game board

    Streamline game;                    // The current level
    ArrayList<Streamline> nextGames;    // Future levels

    MyKeyHandler myKeyHandler;          // for keyboard input


    /**
     *
     * @return the width of the board for the current level
     */

    public int getBoardWidth() {
        return game.currentState.board[0].length;
    }

    /**
     *
     * @return the height of the board for the current level
     */
    public int getBoardHeight() {
        return game.currentState.board.length;
    }

    /**
     * Find a size for a single square of the board that will fit nicely
     * in the current scene size
     * @return the size of a single square
     */
    public double getSquareSize() {
        double sceneWidth = mainScene.getWidth();
        double sceneHeight = mainScene.getHeight();
        double boardWidth = this.getBoardWidth();
        double boardHeight = this.getBoardHeight();

        int width = (int)(sceneWidth/boardWidth);
        int height = (int) (sceneHeight/boardHeight);
        if(width >= height){
            return height;
        }
        else{
            return width;
        }
    }

    /**
     * Destroy and recreate grid and all trail and obstacle shapes
     * when a new level loads.
     */
    public void resetGrid() {
        levelGroup.getChildren().clear();
        this.grid = new Shape[getBoardHeight()][getBoardWidth()];
        for(int i = 0; i < grid.length;i++)
        {
            for (int j = 0; j < grid[0].length; j++)
            {
                double[] center = boardIdxToScenePos(j,i);

                if (game.currentState.board[i][j]
                        == GameState.OBSTACLE_CHAR)
                {
                    grid[i][j] = new RoundedSquare
                            (SQUARE_FRACTION * getSquareSize());
                    grid[i][j].setFill(OBSTACLE_COLOR);

                    ((RoundedSquare)(grid[i][j])).setCenterX(center[0]);
                    ((RoundedSquare)(grid[i][j])).setCenterY(center[1]);
                }
                else if(game.currentState.board[i][j]
                        == GameState.TRAIL_CHAR)
                {
                    grid[i][j] = new Circle
                            (TRAIL_RADIUS_FRACTION * getSquareSize());
                    grid[i][j].setFill(TRAIL_COLOR);

                    ((Circle)(grid[i][j])).setCenterX(center[0]);
                    ((Circle)(grid[i][j])).setCenterY(center[1]);
                }
                else{
                    grid[i][j] = new Circle
                            (TRAIL_RADIUS_FRACTION * getSquareSize());
                    grid[i][j].setFill(Color.TRANSPARENT);

                    ((Circle)(grid[i][j])).setCenterX(center[0]);
                    ((Circle)(grid[i][j])).setCenterY(center[1]);
                }
                levelGroup.getChildren().add(grid[i][j]);

            }
        }
    }

    /**
     * Sets the fill color of all trail Circles making them visible or not
     * depending on if that board position equals TRAIL_CHAR.
     */
    public void updateTrailColors() {
        for(int i = 0; i < grid.length;i++)
        {
            for(int j = 0; j < grid[0].length; j++)
            {
                if(game.currentState.board[i][j]
                        == GameState.TRAIL_CHAR)
                {
                    grid[i][j].setFill(TRAIL_COLOR);
                }

                else if(game.currentState.board[i][j]
                        == GameState.SPACE_CHAR)
                {
                    grid[i][j].setFill(Color.TRANSPARENT);
                }
            }
        }

    }

    /**
     * Coverts the given board column and row into scene coordinates.
     * Gives the center of the corresponding tile.
     * @param boardCol a board column to be converted to a scene x
     * @param boardRow a board row to be converted to a scene y
     * @return scene coordinates as length 2 array where index 0 is x
     */
    static final double MIDDLE_OFFSET = 0.5;
    public double[] boardIdxToScenePos (int boardCol, int boardRow) {
        double sceneX = ((boardCol + MIDDLE_OFFSET) *
            (mainScene.getWidth() - 1)) / getBoardWidth();
        double sceneY = ((boardRow + MIDDLE_OFFSET) *
            (mainScene.getHeight() - 1)) / getBoardHeight();
        return new double[]{sceneX, sceneY};
    }

    /**
     * To be called when the user moved the player and the GUI needs to be
     * updated to show the new position.
     * Makes trail markers visible and changes player position.
     * @param fromCol player old position X
     * @param fromRow player old position Y
     * @param toCol player new position X
     * @param toRow player new position Y
     * @param isUndo whether it was an undo movement
     */
    public void onPlayerMoved(int fromCol, int fromRow, int toCol, int toRow,
        boolean isUndo)
    {

        // If the position is the same, just return
        if (fromCol == toCol && fromRow == toRow) {
            return;
        }

        double[] playerPos = boardIdxToScenePos(toCol,toRow);
        playerRect.setCenterX(playerPos[0]);
        playerRect.setCenterY(playerPos[1]);

        updateTrailColors();

        double[] prevPlayerPos = boardIdxToScenePos(fromCol,fromRow);

        Line line = new Line();
        line.setStartX(prevPlayerPos[0]);
        line.setStartY(prevPlayerPos[1]);
        line.setEndX(playerPos[0]);
        line.setEndY(playerPos[1]);

        PathTransition pathTransition = new PathTransition();
        pathTransition.setNode(playerRect);
        pathTransition.setDuration(Duration.millis(PLAYER_MOVE));
        pathTransition.setPath(line);
        pathTransition.play();


        /*if(fromCol == toCol && fromRow > toRow && toRow>0){
            if(game.currentState.board[toRow-1][toCol]
                    == GameState.OBSTACLE_CHAR)
            {
                double[] OB1 = boardIdxToScenePos(toCol,toRow-1);
                RoundedSquare ob1 = (RoundedSquare)grid[toRow-1][toCol];
                ob1.setCenterX(OB1[0]);
                ob1.setCenterY(OB1[1]);
                PathTransition pt = new PathTransition();
                pt.setNode(ob1);
                pt.setDuration(Duration.millis(PLAYER_MOVE));
                pt.setCycleCount(2);
                pt.setAutoReverse(true);
                pt.play();
            }
        }*/

        if(game.currentState.levelPassed)
        {
            onLevelFinished();
        }
    }

    /**
     * To be called when a key is pressed
     * @param keyCode the input keyCode
     */
    void handleKeyCode(KeyCode keyCode) {
        if(game.currentState.levelPassed)
        {
            return;
        }

        int last = game.previousStates.size()-1;
        switch (keyCode) {
            case DOWN:
                game.recordAndMove(Direction.DOWN);
                break;
            case UP:
                game.recordAndMove(Direction.UP);
                break;
            case LEFT:
                game.recordAndMove(Direction.LEFT);
                break;
            case RIGHT:
                game.recordAndMove(Direction.RIGHT);
                break;
            case U:
                if(last >= 0)
                {
                    onPlayerMoved(game.currentState.playerCol,
                            game.currentState.playerRow,
                            game.previousStates.get(last).playerCol,
                            game.previousStates.get(last).playerRow, true);
                    game.undo();
                    updateTrailColors();
                }
                break;
            case O:
                game.saveToFile();
                break;
            case Q:
                onLevelFinished();

            default:
                System.out.println("Possible commands:\n w - up\n " +
                    "a - left\n s - down\n d - right\n u - undo\n " +
                    "q - quit level");
                break;
        }
        // Call onPlayerMoved() to update the GUI to reflect the player's
        // movement (if any)
        if(game.previousStates.size() == last+1){
            return;
        }
        last = game.previousStates.size()-1;
        if(keyCode == KeyCode.DOWN|| keyCode == KeyCode.UP
                || keyCode == KeyCode.RIGHT||keyCode == KeyCode.LEFT){
                onPlayerMoved(game.previousStates.get(last).playerCol,
                        game.previousStates.get(last).playerRow,
                        game.currentState.playerCol,
                        game.currentState.playerRow, false);
        }
    }

    /**
     * This nested class handles keyboard input and calls handleKeyCode()
     */
    class MyKeyHandler implements EventHandler<KeyEvent> {
        @Override
        public void handle(KeyEvent e) {
            handleKeyCode(e.getCode());
        }
    }

    /**
     * To be called whenever the UI needs to be completely
     * redone to reflect a new level
     */
    public void onLevelLoaded() {
        resetGrid();

        double squareSize = getSquareSize() * SQUARE_FRACTION;

        // Update the player position
        double[] playerPos = boardIdxToScenePos(
            game.currentState.playerCol, game.currentState.playerRow
        );
        playerRect.setSize(squareSize);
        playerRect.setCenterX(playerPos[0]);
        playerRect.setCenterY(playerPos[1]);

        double[] goalPos = boardIdxToScenePos(
                game.currentState.goalCol, game.currentState.goalRow
        );
        goalRect.setSize(squareSize);
        goalRect.setCenterX(goalPos[0]);
        goalRect.setCenterY(goalPos[1]);

    }

    /**
     * To be called when the player reaches the goal.
     * Shows the winning animation and loads the next level if there is one.
     */
    static final double SCALE_TIME = 175;  // milliseconds for scale animation
    static final double FADE_TIME = 250;   // milliseconds for fade animation
    static final double DOUBLE_MULTIPLIER = 2;
    public void onLevelFinished() {
        // Clone the goal rectangle and scale it up until it covers the screen

        // Clone the goal rectangle
        Rectangle animatedGoal = new Rectangle(
            goalRect.getX(),
            goalRect.getY(),
            goalRect.getWidth(),
            goalRect.getHeight()
        );
        animatedGoal.setFill(goalRect.getFill());

        // Add the clone to the scene
        List<Node> children = rootGroup.getChildren();
        children.add(children.indexOf(goalRect), animatedGoal);

        // Create the scale animation
        ScaleTransition st = new ScaleTransition(
            Duration.millis(SCALE_TIME), animatedGoal
        );
        st.setInterpolator(Interpolator.EASE_IN);

        // Scale enough to eventually cover the entire scene
        st.setByX(DOUBLE_MULTIPLIER *
            mainScene.getWidth() / animatedGoal.getWidth());
        st.setByY(DOUBLE_MULTIPLIER *
            mainScene.getHeight() / animatedGoal.getHeight());

        /**
         * This will be called after the scale animation finishes.
         * If there is no next level, quit. Otherwise switch to it and
         * fade out the animated cloned goal to reveal the new level.
         */
        st.setOnFinished(e1 -> {


            //check if there is no next game and if so, quit
            if(nextGames.size() <= 1){
                System.exit(0);
            }
            //update the instances variables game and nextGames
            //to switch to the next level
            nextGames.remove(0);
            game = nextGames.get(0);

            // Update UI to the next level, but it won't be visible yet
            // because it's covered by the animated cloned goal
            onLevelLoaded();

            //use a FadeTransition on animatedGoal, with FADE_TIME as
            //the duration. Use setOnFinished() to schedule code to
            //run after this animation is finished. When the animation
            //finishes, remove animatedGoal from rootGroup.
            FadeTransition fade = new FadeTransition();
            fade.setDuration(Duration.millis(FADE_TIME));
            fade.setNode(animatedGoal);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);

            fade.setOnFinished(event -> {
                rootGroup.getChildren().remove(animatedGoal);
            });
            // Start the fade animation
            fade.play();

        });

        // Start the scale animation
        st.play();
    }

    /**
     * Performs file IO to populate game and nextGames using filenames from
     * command line arguments.
     */
    public void loadLevels() {
        game = null;
        nextGames = new ArrayList<Streamline>();

        List<String> args = getParameters().getRaw();
        if (args.size() == 0) {
            System.out.println("Starting a default-sized random game...");
            game = new Streamline();
            return;
        }

        // at this point args.length == 1

        File file = new File(args.get(0));
        if (!file.exists()) {
            System.out.printf("File %s does not exist. Exiting...",
                args.get(0));
            return;
        }

        // if is not a directory, read from the file and start the game
        if (!file.isDirectory()) {
            System.out.printf("Loading single game from file %s...\n",
                args.get(0));
            game = new Streamline(args.get(0));
            return;
        }

        // file is a directory, walk the directory and load from all files
        File[] subfiles = file.listFiles();
        Arrays.sort(subfiles);
        for (int i=0; i<subfiles.length; i++) {
            File subfile = subfiles[i];

            // in case there's a directory in there, skip
            if (subfile.isDirectory()) continue;

            // assume all files are properly formatted games,
            // create a new game for each file, and add it to nextGames
            System.out.printf("Loading game %d/%d from file %s...\n",
                i+1, subfiles.length, subfile.toString());
            nextGames.add(new Streamline(subfile.toString()));
        }

        // Switch to the first level
        game = nextGames.get(0);
        nextGames.remove(0);
    }

    /**
     * The main entry point for all JavaFX Applications
     * Initializes instance variables, creates the scene, and sets up the UI
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Populate game and nextGames
        loadLevels();

        // Initialize the scene and our groups
        rootGroup = new Group();
        mainScene = new Scene(rootGroup, SCENE_WIDTH, SCENE_HEIGHT,
            Color.GAINSBORO);
        levelGroup = new Group();
        rootGroup.getChildren().add(levelGroup);

        goalRect = new RoundedSquare();
        goalRect.setFill(GOAL_COLOR);
        rootGroup.getChildren().add(goalRect);

        playerRect = new Player();
        rootGroup.getChildren().add(playerRect);

        onLevelLoaded();
        myKeyHandler = new MyKeyHandler();
        mainScene.setOnKeyPressed(myKeyHandler);


        // Make the scene visible
        primaryStage.setTitle(TITLE);
        primaryStage.setScene(mainScene);
        primaryStage.setResizable(false );
        primaryStage.show();
    }

    /**
     * Execution begins here, but at this point we don't have a UI yet
     * The only thing to do is call launch() which will eventually result in
     * start() above being called.
     */
    public static void main(String[] args) {
        if (args.length != 0 && args.length != 1) {
            System.out.print(USAGE);
            return;
        }

        launch(args);
    }
}
