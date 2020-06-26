package com.justchill.android.sudoku;

// Java program to implement Graph
// with the help of Generics

import java.util.*;

// TODO: make code better (refactor, comment etc.)

class Graph<T> {

    // We use Hashmap to store the edges in the graph
    private Map<T, List<T> > map = new HashMap<>();

    // This function adds a new vertex to the graph
    public void addVertex(T s)
    {
        map.put(s, new LinkedList<T>());
    }

    // This function adds the edge
    // between source to destination
    public void addEdge(T source,
                        T destination,
                        boolean bidirectional)
    {

        if (!map.containsKey(source))
            addVertex(source);

        if (!map.containsKey(destination))
            addVertex(destination);

        map.get(source).add(destination);
        if (bidirectional == true) {
            map.get(destination).add(source);
        }
    }

    public void removeEdge(T source, T destination, boolean bidirectional) {
        if(source == null || destination == null) return;
        if(map.get(source) != null) {
            map.get(source).remove(destination);
        }
        if(bidirectional && map.get(destination) != null) {
            map.get(destination).remove(source);
        }
    }

    // This function gives the count of vertices
    public void getVertexCount()
    {
        System.out.println("The graph has "
                + map.keySet().size()
                + " vertex");
    }

    // This function gives the count of edges
    public void getEdgesCount(boolean bidirection)
    {
        int count = 0;
        for (T v : map.keySet()) {
            count += map.get(v).size();
        }
        if (bidirection == true) {
            count = count / 2;
        }
        System.out.println("The graph has "
                + count
                + " edges.");
    }


    // This function gives whether
    // a vertex is present or not.
    public void hasVertex(T s)
    {
        if (map.containsKey(s)) {
            System.out.println("The graph contains "
                    + s + " as a vertex.");
        }
        else {
            System.out.println("The graph does not contain "
                    + s + " as a vertex.");
        }
    }

    // This function gives whether an edge is present or not.
    public boolean hasEdge(T s, T d, boolean bidirectional)
    {
        if(map.get(s) != null) {
            if(map.get(s).contains(d)) return true;
        }
        if(bidirectional && map.get(d) != null) {
            if(map.get(d).contains(s)) return true;
        }
        return false;
    }

    public T getNextVertex(T s) {
        if(map.get(s) == null || map.get(s).isEmpty()) return null;
        return map.get(s).get(0);
    }

    // Prints the adjancency list of each vertex.
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        for (T v : map.keySet()) {
            builder.append(v.toString() + ": ");
            for (T w : map.get(v)) {
                builder.append(w.toString() + " ");
            }
            builder.append("\n");
        }

        return (builder.toString());
    }
}
