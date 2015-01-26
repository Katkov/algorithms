package com.katkov.sciena.onlinejudje.Exercises4;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by eloor_000 on 1/20/2015.
 */
public class BulletAgainstWalls {
    /**
     * read data from the file with name "bullet_against_walls.txt" and
     * parse them to form two main objects of this problem - "origin" and "polygon".
     * Origin is the point from, which the bullet is released.
     * Polygon is the room with walls, which bullet should intersect.
     * Finally invoke method solve(polygon,origin) which will count results and print out information
     * about the direction of bullet, how many walls will bullet intersect, and so on.
     *
     * It is example of the "bullet_against_walls.txt" file view:
     *
     * 3
     * 5
     * -1 -1 -6 -1
     * -6 -1 -4 3
     * -4 3 -3 1
     * -3 1 0 2
     * 0 2 -1 -1
     * 1 -1
     * 4
     * -1 -1 -1 1
     * -1 1 1 1
     * 1 1 1 -1
     * 1 -1 -1 -1
     * 0 0
     * 8
     * 1 0 2 0
     * 2 0 2 2
     * 2 2 -2 2
     * -2 2 -2 0
     * -2 0 -1 0
     * -1 0 -1 1
     * -1 1 1 1
     * 1 1 1 0
     * 0 0
     *
     * The first line contains the number of the tests. There are 3 tests in the example above.
     * The second line contains the number of the edges in polygon for the first test.
     * The 3-7 lines contain coordinates of edges in polygon:
     * For example, the third line describes the edge with coordinates [(-1,-1),(-6,-1)]
     * The 8th line are the coordinates of origin.
     * The 9th line contains the number of edges in polygon for the second test.
     * And so on.
     *
     *
     *
     * @param args standard array of console parameters
     * @throws FileNotFoundException if compiler isn't able to find a file with the name "bullet_against_walls.txt"
     *
     *
     */
    public static void main(String[] args) throws FileNotFoundException{
        System.setIn(new FileInputStream("bullet_against_walls.txt"));

        Scanner scanner = new Scanner(System.in);
        int testsCount = scanner.nextInt();
        for(int t = 0; t < testsCount; t++){
            int edgesCount = scanner.nextInt();
            Polygon polygon = new Polygon();
            for(int i = 0; i < edgesCount; i++) {
                Point start = new Point(scanner.nextInt(), scanner.nextInt(), PointType.START);
                Point end = new Point(scanner.nextInt(), scanner.nextInt(), PointType.END);
                Edge edge = new Edge(start, end);
                polygon.add(edge);
            }
            Point origin = new Point(scanner.nextInt(), scanner.nextInt(), PointType.QUESTION);
            System.out.println("Test number " + (t + 1));
            solve(polygon,origin);

        }

    }

    /**
     * It is the method that solve this problem:
     *
     * Let P be a simple, but not necessarily convex, polygon and q an arbitrary
     * point not necessarily in P. Design an efficient algorithm to find a line segment
     * originating from q that intersects the maximum number of edges of P. In other
     * words, if standing at point q, in what direction should you aim a gun so the bullet
     * will go through the largest number of walls. A bullet through a vertex of P gets
     * credit for only one wall. An O(n log n) algorithm is possible.
     *
     * @param polygon P
     * @param point q
     */
    public static void solve(Polygon polygon, Point point){
        Point shift = new Point(-point.x,-point.y,PointType.SHIFT);
        makeShift(polygon,shift);
        makePolar(polygon);
        makeCounterClockwise(polygon);
        List<Edge> overlappingList = findDirection(polygon);
        System.out.println("A bullet could be realised in any direction between: ");
        for(Edge edge: overlappingList){
                System.out.println((edge.start.phi / Math.PI * 180) + " degree and "
                                    + (edge.end.phi / Math.PI * 180) + " degree");
        }
        double direction = middlePhi(overlappingList.get(0));
        double radius = findMaxDistance(polygon);
        Edge resultSegment = maxEdgesIntersector(direction,radius,point);
        System.out.println(resultSegment.toString() + " intersects maximum number of polygon's edges");

    }

    /**
     * find interval that intersects maximum number of edges of polygon
     * @param direction in which line segment intersects the maximum number of edges
     * @param radius the maximum distance from the origin to the polygon
     * @param origin the coordinates of the origin
     * @return interval that intersects the maximum number of the polygon's edges
     */
    public static Edge maxEdgesIntersector(double direction, double radius, Point origin){
        double x = radius * Math.cos(direction) + origin.x;
        double y = radius * Math.sin(direction) + origin.y;
        return new Edge(origin,new Point((int)Math.round(x),(int)Math.round(y),PointType.QUESTION));
    }
    /**
     * find the middle of the edge in phi coordinates
     * @param edge any Edge
     * @return middle of the edge in phi coordinates
     */
    public static double middlePhi(Edge edge){
        return (edge.start.phi + edge.end.phi) / 2;
    }
    /**
     * Find point in polygon with the maximum distance from origin
     * and return this distance
     * @param polygon any polygon
     * @return the maximum distance from origin to the polygon
     */
    public static double findMaxDistance(Polygon polygon){
        double max = -1;
        for(Edge edge: polygon.edges){
            if(edge.end.r > max){
                max = edge.end.r;
            }
            if(edge.start.r > max){
                max = edge.start.r;
            }
        }
        return max;
    }
    /**
     * Find intervals, which describe directions in which line segments
     * originated from (0,0) intersects the maximum number of edges of polygon.
     * Intervals are intervals of polar angles in radians.
     * @param polygon any polygon
     * @return List of Edges that describes directions in which line segments
     * originated from (0,0) intersects the maximum number of edges of polygon.
     */
    public static List<Edge> findDirection(Polygon polygon){
        Point[] points = makePointsArray(polygon);
        sortPoints(points);
        return findMaxOverlapping(points);
    }

    /**
     * Find List of intervals with maximum overlapping by intervals of polygon,
     * which are represented as sorted list of points.
     * @param points sorted by phi list of points
     * @return Find List of intervals with maximum overlapping by intervals of polygon,
     * which are represented as sorted list of points.
     */
    public static List<Edge> findMaxOverlapping(Point[] points){
        int counter = 0;
        List<Integer> maxIndexes = new ArrayList<Integer>();
        int max = -1;
        for(int i = 0; i < points.length; i++){
            if(points[i].type == PointType.START){
                counter++;
                if(counter > max){
                    max = counter;
                    maxIndexes.clear();
                    maxIndexes.add(i);
                } else if(counter == max){
                    maxIndexes.add(i);
                }
            }else if(points[i].type == PointType.END){
                counter--;
            }
        }
        List<Edge> result = new ArrayList<Edge>();
        for(int a: maxIndexes){
            Edge edge;
            if(a == points.length - 1){
                edge = new Edge(points[a], points[0]);
            } else {
                edge = new Edge(points[a], points[a + 1]);
            }
            result.add(edge);
        }
        return result;
    }

    /**
     * sort array of points by phi in ascendant order,
     * if for two points from array phi values are equal, then
     * assume the point with PointType.END as smaller from this two points.
     * @param points array of points
     */
    public static void sortPoints(Point[] points){
        Arrays.sort(points, new Comparator<Point>() {
            @Override
            public int compare(Point point1, Point point2) {
                if(point1.phi < point2.phi){
                    return -1;
                } else if(point2.phi > point1.phi){
                    return 1;
                } else if(point1.phi == point2.phi && point1.type == PointType.END){
                    return -1;
                } else if(point1.phi == point2.phi && point2.type == PointType.END){
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }

    /**
     * store all points of polygon into array of points
     * @param polygon any polygon
     * @return array of points
     */
    public static Point[] makePointsArray(Polygon polygon){
        int arraySize = polygon.edges.size();
        Point[] points = new Point[2 * arraySize];
        int counter = 0;
        for(Edge edge: polygon.edges){
            points[counter] = edge.start;
            counter++;
            points[counter] = edge.end;
            counter++;
        }
        return points;
    }

    /**
     * Make all edges so they would be directed counterclockwise
     * @param polygon any polygon
     */
    public static void makeCounterClockwise(Polygon polygon){
        for (Edge edge: polygon.edges){
            if(isNeedToSwapVertices(edge)){
                swapVertices(edge);
            }
        }
    }

    /**
     * Check if the start lies after the end of the edge in
     * counterclockwise direction or otherwise
     * @param edge any Edge
     * @return true if the start lies after the end of the edge in
     * counterclockwise direction, or false if the start lies before the end
     * of the edge in counterclockwise direction.
     */
    public static boolean isNeedToSwapVertices(Edge edge){
        //it is cross product of two vectors edge.start and edge.end
        return edge.start.x * edge.end.y - edge.start.y * edge.end.x < 0;
    }

    /**
     * Usually any edge has the start and the end of it.
     * This method helps to swap the start and the and of the edge.
     * @param edge any Edge
     */
    public static void swapVertices(Edge edge){
        Point temp = edge.start;
        edge.start = edge.end;
        edge.end = temp;
        edge.start.type = PointType.START;
        edge.end.type = PointType.END;
    }

    /**
     * Count polar coordinates of the all points of polygon
     * and write them to corresponding r and phi coordinates of the
     * Point object
     * @param polygon any polygon
     */
    public static void makePolar(Polygon polygon){
        for(Edge edge: polygon.edges){
            edge.start.makePolar();
            edge.end.makePolar();
        }
    }

    /**
     * Move all points of polygon into (shift.x,shift.y)
     * @param polygon any polygon
     * @param shift is a point or vector that describes coordinates of the shift in plane
     */
    public static void makeShift(Polygon polygon, Point shift){
        for(Edge edge: polygon.edges){
            edge.start.x += shift.x;
            edge.start.y += shift.y;
            edge.end.x += shift.x;
            edge.end.y += shift.y;
        }
    }

    /**
     * PointType describe type of Point
     * START means the start of an interval
     * END means the end of an interval
     * QUESTION is a target point
     * SHIFT is a shift
     */
    public enum PointType{
        START, END, QUESTION, SHIFT
    }

    /**
     * Class described a point on the plane
     */
    public static class Point{
        int x;
        int y;
        PointType type;
        //polar coordinates;
        double r;
        double phi;

        public Point(int x, int y, PointType type){
            this.x = x;
            this.y = y;
            this.type = type;
        }

        /**
         * count polar coordinates for this point.
         * Write radius to r, and angle to phi
         */
        public void makePolar(){
            r = Math.sqrt(x*x + y*y);
            if(x >= 0 && y == 0){
                phi = 0;
            } else if(x > 0 && y > 0){
                phi = Math.atan((double)y/(double)x);
            } else if(x == 0 && y > 0){
                phi = Math.PI / 2;
            } else if(x < 0 && y > 0){
                phi = Math.PI + Math.atan((double)y/(double)x);
            } else if(x < 0 && y == 0){
                phi = Math.PI;
            } else if(x < 0 && y < 0){
                phi = Math.PI + Math.atan((double)y/(double)x);
            } else if(x == 0 & y < 0){
                phi = 3 * Math.PI / 2;
            } else if(x > 0 && y < 0){
                phi = 2 * Math.PI + Math.atan((double)y/(double)x);
            }
        }

        @Override
        public String toString() {
            return "(" + x + "," + y + ")";
        }
    }

    /**
     * It is interval that contains two points
     */
    public static class Edge{
        Point start;
        Point end;

        public Edge(Point start, Point end){
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return "[" + start.toString() + "," + end.toString() + "]";
        }
    }

    /**
     * It is Polygon that consist of the list of edges
     */
    public static class Polygon {
        List<Edge> edges;

        public Polygon(){
            edges = new ArrayList<Edge>();
        }

        public void add(Edge edge){
            edges.add(edge);
        }
    }
}
