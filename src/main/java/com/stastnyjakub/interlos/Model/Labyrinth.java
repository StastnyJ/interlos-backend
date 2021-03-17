package com.stastnyjakub.interlos.Model;

import java.util.Arrays;

public class Labyrinth {
    private static class Position {
        public enum Orientation {
            UP, DOWN, LEFT, RIGHT, UNDEFINED
        }

        private final Integer x;
        private final Integer y;
        private final Orientation orientation;

        public Position(Integer x, Integer y, Orientation orientation) {
            this.x = x;
            this.y = y;
            this.orientation = orientation;
        }

        public Position(Integer x, Integer y) {
            this(x, y, Orientation.UNDEFINED);
        }

        public Integer getX() {
            return this.x;
        }

        public Integer getY() {
            return this.y;
        }

        public Position getNextPosition() {
            if (this.orientation == Orientation.LEFT)
                return new Position(this.x, this.y - 1, this.orientation);
            if (this.orientation == Orientation.RIGHT)
                return new Position(this.x, this.y + 1, this.orientation);
            if (this.orientation == Orientation.UP)
                return new Position(this.x - 1, this.y, this.orientation);
            if (this.orientation == Orientation.DOWN)
                return new Position(this.x + 1, this.y, this.orientation);

            throw new IllegalStateException();
        }

        public Position turnLeft() {
            if (this.orientation == Orientation.LEFT)
                return new Position(this.x, this.y, Orientation.DOWN);
            if (this.orientation == Orientation.RIGHT)
                return new Position(this.x, this.y, Orientation.UP);
            if (this.orientation == Orientation.UP)
                return new Position(this.x, this.y, Orientation.LEFT);
            if (this.orientation == Orientation.DOWN)
                return new Position(this.x, this.y, Orientation.RIGHT);

            throw new IllegalStateException();
        }

        public Position turnRight() {
            return turnLeft().turnLeft().turnLeft();
        }
    }

    private enum TileType {
        WALL, FREE
    }

    public enum StepResult {
        NORMAL, WALL_HIT, GOAL_REACHED
    }

    private final TileType[][] map;
    private final Position goal;

    private Position actualPosition;

    private Labyrinth(TileType[][] map, Position start, Position goal) {
        this.map = map;
        this.actualPosition = start;
        this.goal = goal;
    }

    public static Labyrinth getTaskLabyrinth() {
        String[] rawMap = { // ----------------------------------------------------
                "######### ######", // 0 -----------------------------------------
                "#   ###   #### #", // 1 -----------------------------------------
                "#       ##  #  #", // 2 -----------------------------------------
                "######  #     ##", // 3 -----------------------------------------
                "#       #  # ###", // 4 -----------------------------------------
                "### ##### ######", // 5 -----------------------------------------
                "#     ##    ## #", // 6 -----------------------------------------
                "#### #### ## # #", // 7 -----------------------------------------
                "#              #", // 8 -----------------------------------------
                "############## #", // 9 -----------------------------------------
                "#           ## #", // 10 -----------------------------------------
                "## #######     #", // 11 -----------------------------------------
                "#  ##  ##  ##  #", // 12 -----------------------------------------
                "# ### ####     #", // 13 -----------------------------------------
                "#      #   ### #", // 14 -----------------------------------------
                "################" // 15 -----------------------------------------
        };

        TileType[][] map = Arrays
                .stream(rawMap).map(row -> row.chars().mapToObj(c -> (char) c)
                        .map(c -> c == '#' ? TileType.WALL : TileType.FREE).toArray(TileType[]::new))
                .toArray(TileType[][]::new);

        return new Labyrinth(map, new Position(14, 3, Position.Orientation.UP), new Position(0, 9));
    }

    public Boolean isWallBeforeMe() {
        Position next = actualPosition.getNextPosition();
        return map[next.getX()][next.getY()] == TileType.WALL;
    }

    public void turnLeft() {
        this.actualPosition = this.actualPosition.turnLeft();
    }

    public void turnRight() {
        this.actualPosition = this.actualPosition.turnRight();
    }

    public StepResult stepForward() {
        if (isWallBeforeMe())
            return StepResult.WALL_HIT;
        this.actualPosition = this.actualPosition.getNextPosition();
        if (actualPosition.getX().equals(goal.getX()) && actualPosition.getY().equals(goal.getY()))
            return StepResult.GOAL_REACHED;
        return StepResult.NORMAL;
    }

    public StepResult stepBackward() {
        turnLeft();
        turnLeft();
        StepResult res = stepForward();
        turnLeft();
        turnLeft();
        return res;
    }
}
