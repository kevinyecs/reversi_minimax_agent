///Maxiking,yekevin12@gmail.com
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import game.engine.utils.Utils;
import game.oth.OthelloAction;

import game.oth.OthelloGame;
import game.oth.OthelloPlayer;

/**
 * An implementation of the {@link OthelloPlayer} that selects the cell of the
 * board for an action that maximizes the number of flipped disks.
 */
public class Agent extends OthelloPlayer {

    public static int POSITIVE_INFINITY = Integer.MAX_VALUE;
    public static int NEGATIVE_INFINITY = Integer.MIN_VALUE;
    /** Search Depth for minimax algorithm*/
    public static int WHITE_SEARCH_DEPTH = 4;
    public static int BLACK_SEARCH_DEPTH = 4;
    public int SEARCH_DEPTH;
    private OthelloAction bestMove = null;

    /** Array for all possible actions. */
    private ArrayList<OthelloAction> actions = new ArrayList<>();

    public Agent(int color, int[][] board, Random random) {
        super(color, board, random);
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                actions.add(new OthelloAction(i, j));
            }
        }
    }

    @Override
    public OthelloAction getAction(OthelloAction prevAction, long[] remainingTimes) {
        if (prevAction != null) {
            OthelloGame.setAction(board, prevAction.i, prevAction.j, 1 - color);
        }

        if(color == OthelloGame.BLACK) SEARCH_DEPTH = BLACK_SEARCH_DEPTH;
        if(color == OthelloGame.WHITE) SEARCH_DEPTH = WHITE_SEARCH_DEPTH;


        /**Activate Greedy if remaining time less then 200 ms */
        /**Sometimes search can take a while
         * if remaining time is 0 then the program crashes/lose
         * so this part makes a fast decision before it crashes
         * */
        if (remainingTimes[color] < 200 ) {
            Collections.shuffle(actions, random);
            OthelloAction action = null;
            int flipped = 0;
            for (OthelloAction a : actions) {
                if (OthelloGame.isValid(board, a.i, a.j, color)) {
                    int [][] newBoard = Utils.copy(board);
                    OthelloGame.setAction(newBoard, a.i, a.j, color);
                    int f = heuristic(newBoard);
                    if (flipped < f) {
                        OthelloGame.setAction(board, a.i, a.j, color);
                        flipped = f;
                        action = a;

                    }
                }
            }
            OthelloGame.setAction(board, action.i, action.j, color);
            return action;
        }


/**Start minimax search as Max players*/
        int bestValue = Integer.MIN_VALUE;
        /** Better performance with shuffle*/
        Collections.shuffle(actions, random);
        for(OthelloAction a : actions){
            if(OthelloGame.isValid(board,a.i,a.j, color)){
                int[][] newBoard = Utils.copy(board);
                OthelloGame.setAction(newBoard, a.i, a.j,color);
               int val = minimax(newBoard ,SEARCH_DEPTH ,NEGATIVE_INFINITY, POSITIVE_INFINITY, false, 1-color);
                if(val > bestValue){
                    /**If minimax found a better value update best value and set action*/
                    bestMove = a;
                    bestValue = val;
                }
            }
        }
        /**Sets action if minimax move is valid*/
        if(OthelloGame.isValid(board, bestMove.i, bestMove.j, color)){
            OthelloGame.setAction(board, bestMove.i, bestMove.j, color);
            return bestMove;
        }else{
            /**Activate Greedy if minimax move not valid for some reason*/
            Collections.shuffle(actions, random);
            OthelloAction action = null;
            int flipped = 0;
            for (OthelloAction a : actions) {
                if (OthelloGame.isValid(board, a.i, a.j, color)) {
                    int [][] newBoard = Utils.copy(board);
                    int f = OthelloGame.setAction(newBoard, a.i, a.j, color);
                    if (flipped < f) {
                        flipped = f;
                        action = a;

                    }
                }
            }
            OthelloGame.setAction(board, action.i, action.j, color);
            return action;
        }




    }



    public int minimax(int[][] board, int depth,int alpha, int beta, boolean isMaximizing, int currentColor){
/** return heuristical value of the board if recursion depth reaches 0*/
        if (depth == 0 ){
            return heuristic(board);
        }
        /** Run max search if current player is max*/
        if (isMaximizing){
            int bestScore = NEGATIVE_INFINITY;

            for (OthelloAction a : actions){
                if (OthelloGame.isValid(board, a.i, a.j, currentColor)) {
                    int[][] newBoard = Utils.copy(board);
                    OthelloGame.setAction(newBoard, a.i, a.j, currentColor);
                    int score = minimax(newBoard, depth - 1,alpha, beta,false, 1-currentColor);
                    if(score > bestScore){
                        bestScore = score;
                    }
                    alpha = Math.max(alpha, score);
                    if( beta <= alpha) break;
                }
            }
            return bestScore;
        }
        /** Run min search if current player is min*/
        else{
            int bestScore = POSITIVE_INFINITY;
            for(OthelloAction a : actions){
                if (OthelloGame.isValid(board, a.i, a.j, currentColor)) {

                    int[][] newBoard = Utils.copy(board);
                    OthelloGame.setAction(newBoard, a.i, a.j, currentColor);
                    int score = minimax(newBoard, depth - 1, alpha, beta, true, 1-currentColor);
                    if(score < bestScore){
                        bestScore = score;
                    }

                    beta = Math.min(beta, score);
                    if (beta <= alpha) break;
                }
            }
            return bestScore;
        }

    };
    /** Evaulation of the board by counting disks*/
    public int heuristic(int[][] board){

        int maxScore = 0, minScore = 0;

        int maxCorners = 0, minCorners = 0;
        for (int i = 0; i < board.length; i++){
            for(int j = 0; j < board[i].length; j++){
                if(board[i][j] == color){
                    maxScore++;
                }
                if(board[i][j] == 1-color){
                    minScore++;
                }
            }
        }



        return 100*(maxScore - minScore)/(maxScore+minScore);


    }
}
