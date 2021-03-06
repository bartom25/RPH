package rph.labyrinth.players;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import rph.labyrinth.Path;
import rph.labyrinth.Player;

public class iliusvlaPlayer extends Player {

    @Override
    protected String getName() {
        return "iliusvlaPlayer";
    }
    public ArrayList<Point> coordinates;
    public static int pole[][];
     
    static Path smile = new Path(Color.YELLOW);
    public static int WIDTH;
    public static int HEIGHT;
    public boolean[][] walls;
    public Node s = new Node(1, 1);
    public Node f = new Node(1, 1);

    @Override
    protected Path findPath(int[][] map) {
        smile = new Path(Color.YELLOW);
        WIDTH = map.length;
        HEIGHT = map[0].length;
        s = new Node(1, 1);
        f = new Node(1, 1);
        walls = new boolean[WIDTH][HEIGHT];
        smile = new Path(Color.YELLOW);
        pole = map;
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map.length; j++) {
                if (map[i][j] == 255) {         
                    f.x = i;
                    f.y = j;           
                }
                if (map[i][j] == 16711680) {           
                    s.x = i;
                    s.y = j;             
                }
                if (map[i][j] == 0) {
                    walls[i][j] = true;
                } else {
                    walls[i][j] = false;
                }
            }
        }


        System.out.println();

        Pathfinder p = new Pathfinder();
        List<Node> pa;
        pa = p.generate(s, f, walls);
        for (Node n : pa) {
            smile.addCoordinate(n.x, n.y);
        }
  
 
        return smile;
    }
}

class Node {

    int x, y;

    public Node(int i, int j) {
        x = i;
        y = j;
    }

    public boolean equals(Object obj) {
        if (obj instanceof Node) {
            Node n = (Node) obj;
            return (x == n.x && y == n.y);
        } else {
            return false;
        }
    }
}

class Pathfinder {

    public static boolean canCutCorners = true;
    private static Node end;
    private static int[][] gScore;
    private static int[][] hScore;
    private static int[][] fScore;
    private static Node[][] cameFrom;
    private static boolean[][] walls;

    public static Node toNode(int i, int j) {
        return new Node(i, j);
    }

    public static List<Node> generate(int startX, int startY, int endX, int endY, boolean[][] mapWalls) {
        return generate(toNode(startX, startY), toNode(endX, endY), mapWalls);
    }

    public static List<Node> generate(Node start, Node finish, boolean[][] mapWalls) {
        List<Node> openNodes = new ArrayList<Node>();
        List<Node> closedNodes = new ArrayList<Node>();
        walls = mapWalls;
        end = finish;
        gScore = new int[walls.length][walls[0].length];
        fScore = new int[walls.length][walls[0].length];
        hScore = new int[walls.length][walls[0].length];
        cameFrom = new Node[walls.length][walls[0].length];
        openNodes.add(start);
        gScore[start.x][start.y] = 0;
        hScore[start.x][start.y] = calculateHeuristic(start);
        fScore[start.x][start.y] = hScore[start.x][start.y];

        while (openNodes.size() > 0) {
            Node current = getLowestNodeIn(openNodes);
            if (current == null) {
                break;
            }
            if (current.equals(end)) {
                return reconstructPath(current);
            }
            System.out.println(current.x + ", " + current.y);

            openNodes.remove(current);
            closedNodes.add(current);

            List<Node> neighbors = getNeighborNodes(current);
            for (Node n : neighbors) {

                if (closedNodes.contains(n)) {
                    continue;
                }

                int tempGscore = gScore[current.x][current.y] + distanceBetween(n, current);

                boolean proceed = false;
                if (!openNodes.contains(n)) {
                    openNodes.add(n);
                    proceed = true;
                } else if (tempGscore < gScore[n.x][n.y]) {
                    proceed = true;
                }

                if (proceed) {
                    cameFrom[n.x][n.y] = current;
                    gScore[n.x][n.y] = tempGscore;
                    hScore[n.x][n.y] = calculateHeuristic(n);
                    fScore[n.x][n.y] = gScore[n.x][n.y] + hScore[n.x][n.y];
                }
            }
        }
        return new ArrayList<Node>();
    }

    private static List<Node> reconstructPath(Node n) {
        if (cameFrom[n.x][n.y] != null) {
            List<Node> path = reconstructPath(cameFrom[n.x][n.y]);
            path.add(n);
            return path;
        } else {
            List<Node> path = new ArrayList<Node>();
            path.add(n);
            return path;
        }
    }

    static boolean outOfBounds(int x, int y) {

        return x < 0 || y < 0 || x >= iliusvlaPlayer.WIDTH || y >= iliusvlaPlayer.WIDTH;
    }

    private static List<Node> getNeighborNodes(Node n) {
        int[] dxArr = new int[]{-1, 0, 1};
        int[] dyArr = new int[]{-1, 0, 1};
        List<Node> found = new ArrayList<Node>();
        for (int i : dxArr) {
            for (int j : dyArr) {
                if (outOfBounds(n.x + i, n.y + j) || i == j || -i == j || i == -j) {
                    continue;
                }
                if (!walls[n.x + i][n.y + j]) {
                    found.add(toNode(n.x + i, n.y + j));
                }
            }
        }

        return found;
    }

    private static Node getLowestNodeIn(List<Node> nodes) {
        int lowest = -1;
        Node found = null;
        for (Node n : nodes) {
            int dist = cameFrom[n.x][n.y] == null ? -1 : gScore[cameFrom[n.x][n.y].x][cameFrom[n.x][n.y].y] + distanceBetween(n, cameFrom[n.x][n.y]) + calculateHeuristic(n);
            if (dist <= lowest || lowest == -1) {
                lowest = dist;
                found = n;
            }
        }
        return found;
    }

    private static int distanceBetween(Node n1, Node n2) {
        return (int) Math.round(10 * Math.sqrt(Math.pow(n1.x - n2.x, 2) + Math.pow(n1.y - n2.y, 2)));
    }

    private static int calculateHeuristic(Node start) {
        return 10 * (Math.abs(start.x - end.x) + Math.abs(start.y - end.y));
    }
}