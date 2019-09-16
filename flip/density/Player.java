package flip.density;

import java.util.List;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.HashMap;
import java.util.ArrayList;
import java.lang.Math;

import javafx.util.Pair;


import flip.sim.Point;
import flip.sim.Board;
import flip.sim.Log;


public class Player implements flip.sim.Player {
    private int seed = 42;
    private Random random;
    private boolean isplayer1;
    private Integer n;
    private Double diameter_piece;
    private Double distance = 5.0;
    private Integer threadfold = 30;

    private enum State {
        INITIAL_STATE,
        WALL,
        FORWARD,
    }

    ;

    private State currentState = State.INITIAL_STATE;

    public Player() {
        random = new Random(seed);
    }

    // Initialization function.
    // pieces: Location of the pieces for the player.
    // n: Number of pieces available.
    // t: Total turns available.
    public void init(HashMap<Integer, Point> pieces, int n, double t, boolean isplayer1, double diameter_piece) {
        this.n = n;
        this.isplayer1 = isplayer1;
        this.diameter_piece = diameter_piece;
    }

    public List<Pair<Integer, Point>> getMoves(Integer num_moves, HashMap<Integer, Point> player_pieces, HashMap<Integer, Point> opponent_pieces, boolean isplayer1) {
        updateState();

        List<Pair<Integer, Point>> moves = new ArrayList<Pair<Integer, Point>>();

        switch (currentState) {
            case WALL: {
                moves = getWallMoves(moves, num_moves, player_pieces, opponent_pieces);
            }
            case FORWARD: {
                moves = getDensityMoves(moves, num_moves, player_pieces, opponent_pieces);
            }
        }

        moves = getRandomMoves(moves, num_moves, player_pieces, opponent_pieces);

        return moves;
    }

    public void updateState() {
        switch (currentState) {
            case INITIAL_STATE: {
                if (checkIfWallStrategyShouldBeUsed()) {
                    currentState = State.WALL;
                } else {
                    currentState = State.FORWARD;
                }
            }
        }
    }

    public boolean checkIfWallStrategyShouldBeUsed() {
        return false;
    }

    double[] wallPointCenters11 = {
            -17.268, -13.804, -10.34, -6.876, -3.412, 0.052, 3.516, 6.98, 10.444, 13.908, 17.372
    }

    private double[] wallPointCenters12 = {
            -18.0, -15.0, -12.0, -9.0, -6.0, -3.0, 3.0, 6.0, 9.0, 12.0, 15.0, 18.0
    }

    public List<Pair<Integer, Point>> getWallMoves(
            List<Pair<Integer, Point>> moves,
            Integer num_moves,
            HashMap<Integer, Point> player_pieces,
            HashMap<Integer, Point> opponent_pieces
    ) {
        double wallX = 40;
        int[] coinsConnections = new int[12];
        List<Integer> usedCoins;
        Stack<Integer> positionsToRecalculate = new Stack<Integer>({0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11});
        for (int i = 0; i < positionsToRecalculate.length; i++) {
            int positionIndex = positionsToRecalculate.pop();
            double y = wallPointCenters12[positionIndex];
            Pair<Integer, Double> coinDistancePair = findClosesPoint(player_pieces, wallX, y, usedCoins);
            Integer coin = coinDistancePair.getKey();
            Double coinDistance = coinDistancePair.getValue();
            usedCoins.add(coin);
            coinsConnections[positionIndex] = coin;
        }
        boolean[] coinsSet = new boolean[12] (false);
        for (int i = 0; i < coinsSet.length; i++) {
            if (coinsSet[i]) {
                continue;
            }
            int coin = coinsConnections[i]
            double y = wallPointCenters12[i];
            moves = findMovesToPoint(
                    coin, new Point(wallX, y), moves, num_moves, player_pieces, opponent_pieces
            );
            if(moves.length >= num_moves) {
                break;
            }
        }
        return moves
    }

    private Pair<Integer, Double> findClosesPoint(
            HashMap<Integer, Point> player_pieces, double x, double y, List<Integer> usedCoins
    ) {
        double minDistance = 120;
        Integer minCoinIndex = -1;
        for (int i = 0; i < n; i++) {
            Point point = player_pieces.get(i);
            if (usedCoins.contains(i)) {
                continue;
            }
            distance = Math.sqrt(Math.pow(x - point.x, 2) + Math.pow(y - point.y, 2));
            if (distance < minDistance) {
                minDistance = distance;
                minCoinIndex = i;
            }
        }
        return new Pair(minCoinIndex, minDistance)
    }

    public List<Pair<Integer, Point>> findMovesToPoint(
            Integer coin,
            Point goal,
            Double allowedError,
            List<Pair<Integer, Point>> moves,
            Integer num_moves,
            HashMap<Integer, Point> player_pieces,
            HashMap<Integer, Point> opponent_pieces
    ) {
        Point coinPoint = player_pieces.get(coin);
        Point newPosition = new Point(coinPoint);
        boolean goalReached = false;
        while(!goalReached && moves.length < num_moves) {
            Point newPosition = new Point(newPosition);
            double distance = getDistance(newPosition, goal);
            if(distance > diameter_piece * 1.5) {
                double theta = getAngle(newPosition, goal);
                double deltaX = diameter_piece * Math.cos(theta);
                double deltaY = diameter_piece * Math.sin(theta);

                newPosition.x = isplayer1 ? newPosition.x - delta_x : newPosition.x + delta_x;
                newPosition.y += delta_y;
                Pair<Integer, Point> move = new Pair<Integer, Point>(coin, newPosition);
                if(check_validity(move, player_pieces, opponent_pieces)) {
                    moves.append(move);
                }
            } else {
                if(distance % 2 < allowedError) {
                    double theta = getAngle(newPosition, goal);
                    double deltaX = diameter_piece * Math.cos(theta);
                    double deltaY = diameter_piece * Math.sin(theta);

                    newPosition.x = isplayer1 ? newPosition.x - delta_x : newPosition.x + delta_x;
                    newPosition.y += delta_y;
                    Pair<Integer, Point> move = new Pair<Integer, Point>(coin, newPosition);
                    if(check_validity(move, player_pieces, opponent_pieces)) {
                        moves.append(move);
                    }
                } else {

                    Pair<Integer, Point> move = new Pair<Integer, Point>(coin, newPosition);
                    if(check_validity(move, player_pieces, opponent_pieces)) {
                        moves.append(move);
                    }
                }
            }
        }
    }

    public double getDistance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    public double getAngle(Point v1, Point v2) {
        return Math.atan2(v1.x * v2.y - v1.y * v2.x, v1.x * v2.x + v1.y * v2.y);
    }

    public double getAbsoluteAngle(Point v) {
        Point u = new Point(0, 1);
        return getAngle(v, u);
    }

    public List<Pair<Integer, Point>> getDensityMoves(
            List<Pair<Integer, Point>> moves,
            Integer num_moves,
            HashMap<Integer, Point> player_pieces,
            HashMap<Integer, Point> opponent_pieces
    ) {
        int sign = isplayer1 ? -1 : 1;
        int low1 = Integer.MAX_VALUE, low2 = Integer.MAX_VALUE, low1Id = -1, low2Id = -1;
        for (int i = 0; i < n; i++) {
            Point curr_position = player_pieces.get(i);
            Pair<Integer, Point> move = new Pair<Integer, Point>(i, new Point(curr_position.x + sign * 2, curr_position.y));
            if (((isplayer1 && curr_position.x < -threadfold)
                    || (!isplayer1 && curr_position.x > threadfold)) || !check_validity(move, player_pieces, opponent_pieces))
                continue;
            int count = 0;
            for (Point point : opponent_pieces.values()) {
                double y = point.y;
                if (y > curr_position.y - distance && y < curr_position.y + distance)
                    count++;
            }
            if (low1 > count) {
                low2 = low1;
                low2Id = low1Id;
                low1 = count;
                low1Id = i;
            } else if (low2 > count) {
                low2 = count;
                low2Id = i;
            }
        }
        if (low1Id != -1) {
            Point point1 = player_pieces.get(low1Id);
            Pair<Integer, Point> move1 = new Pair<Integer, Point>(low1Id, new Point(point1.x + sign * 2, point1.y));
            moves.add(move1);
            Pair<Integer, Point> move2 = new Pair<Integer, Point>(low1Id, new Point(point1.x + sign * 4, point1.y));
            if (check_validity(move2, player_pieces, opponent_pieces))
                moves.add(move2);
            else if (low2Id != -1) {
                Point point2 = player_pieces.get(low2Id);
                Pair<Integer, Point> move3 = new Pair<Integer, Point>(low2Id, new Point(point2.x + sign * 2, point2.y));
                moves.add(move3);
            }
        }

        return moves;
    }

    public List<Pair<Integer, Point>> getRandomMoves(
            List<Pair<Integer, Point>> moves,
            Integer num_moves,
            HashMap<Integer, Point> player_pieces,
            HashMap<Integer, Point> opponent_pieces
    ) {
        int num_trials = 30;
        int i = 0;

        while (moves.size() != num_moves && i < num_trials) {

            Integer piece_id = random.nextInt(n);

            Point curr_position = player_pieces.get(piece_id);
            if (((isplayer1 && curr_position.x < -threadfold)
                    || (!isplayer1 && curr_position.x > threadfold))) continue;
            Point new_position = new Point(curr_position);
            double theta = -Math.PI / 2 + Math.PI * random.nextDouble();
            double delta_x = diameter_piece * Math.cos(theta);
            double delta_y = diameter_piece * Math.sin(theta);

            Double val = (Math.pow(delta_x, 2) + Math.pow(delta_y, 2));

            new_position.x = isplayer1 ? new_position.x - delta_x : new_position.x + delta_x;
            new_position.y += delta_y;
            Pair<Integer, Point> move = new Pair<Integer, Point>(piece_id, new_position);

            Double dist = Board.getdist(player_pieces.get(move.getKey()), move.getValue());

            if (check_validity(move, player_pieces, opponent_pieces))
                moves.add(move);
            i++;
        }

        return moves;
    }


    public boolean check_validity(Pair<Integer, Point> move, HashMap<Integer, Point> player_pieces, HashMap<Integer, Point> opponent_pieces) {
        boolean valid = true;

        // check if move is adjacent to previous position.
        if (!Board.almostEqual(Board.getdist(player_pieces.get(move.getKey()), move.getValue()), diameter_piece)) {
            return false;
        }
        // check for collisions
        valid = valid && !Board.check_collision(player_pieces, move);
        valid = valid && !Board.check_collision(opponent_pieces, move);

        // check within bounds
        valid = valid && Board.check_within_bounds(move);
        return valid;

    }
}

