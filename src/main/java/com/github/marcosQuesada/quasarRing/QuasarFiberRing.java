package com.github.marcosQuesada.quasarRing;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.Strand;

public class QuasarFiberRing {

    protected int iterations;
    protected int nodesOnRing;

    public QuasarFiberRing(int iterations, int nodesOnRing) {
        this.iterations = iterations;
        this.nodesOnRing = nodesOnRing;
    }

    class Node extends Fiber<Integer> {
        protected Node nextNode;
        protected int result;
        protected boolean wait = true;

        public Node(int id) {

            super(String.format("%s-%d", Node.class.getSimpleName(), id));
        }

        @Override
        public Integer run() throws SuspendExecution, InterruptedException {
            do {
                while (wait) {
                    Strand.park();
                }
                wait = true;
                nextNode.result = result - 1;
                nextNode.wait = false;
                Strand.unpark(nextNode);
            } while (result > 0);
            return result;
        }
    }


    public int[] makeRing() throws Exception {
        // Create fibers.
        final Node[] nodes = new Node[nodesOnRing];
        for (int i = 0; i < nodesOnRing; i++)
            nodes[i] = new Node(i);

        // Set next fiber pointers.
        for (int i = 0; i < nodesOnRing; i++)
            nodes[i].nextNode = nodes[(i + 1) % nodesOnRing];

        // Start fibers.
        for (final Node node : nodes)
            node.start();

        // Initiate the ring.
        final Node first = nodes[0];
        first.result = iterations;
        first.wait = false;
        Strand.unpark(first);

        // Wait for fibers to complete.
        final int[] results = new int[iterations];
        for (int i = 0; i < iterations; i++)
            results[i] = nodes[i].get();


        return results;
    }

}

