package com.github.marcosQuesada.quasarRing;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.Channels;
import co.paralleluniverse.strands.channels.Selector;


public class QuasarFiberChannelRing {
    protected int iterations;
    protected int nodesOnRing;

    public QuasarFiberChannelRing(int nodesOnRing, int iterations) {
        this.nodesOnRing = nodesOnRing;
        this.iterations = iterations;
    }

    class Node extends Fiber<Integer> {
        protected int id;
        protected Node nextNode;
        protected Channel<Integer> msgChan;
        protected int iterations;

        public Node(int id) {
            super(String.format("%s-%d", Node.class.getSimpleName(), id));
            this.msgChan = Channels.newChannel(0);
            this.id = id;
        }

        @Override
        public Integer run() throws SuspendExecution, InterruptedException {
            Integer rcv;
            for (; ; ) {
                rcv = msgChan.receive();
                System.out.println(String.format("%d recived: %d", id, rcv));

                if (id == 0) {
                    rcv++; //iteration done!
                    System.out.println("Iteration Done" + String.format("%d", rcv));
                }

                nextNode.msgChan.send(rcv);

                if (rcv == iterations) {
                    System.out.println("Iterations achieved, exiting " + String.format("%d", id));
                    break;
                }
            }

            return rcv;
        }
    }

    public int[] doRing() throws Exception {
        // Create fibers.
        Node[] nodes = new Node[nodesOnRing];
        for (int i = 0; i < nodesOnRing; i++) {
            nodes[i] = new Node(i);
        }

        // Set nextNode fiber pointers.
        for (int i = 0; i < nodesOnRing; i++) {
            nodes[i].nextNode = nodes[(i + 1) % nodesOnRing];
            nodes[i].iterations = iterations;
        }

        for (Node node : nodes)
            node.start();

        // Initiate the ring.
        Node first = nodes[0];
        first.msgChan.send(0);

        System.out.println("DOOOONE");

        // Wait for actors to finish and collect the results.
        int[] sequences = new int[nodesOnRing];
        //for (int i = 0; i < nodesOnRing; i++)
          //  sequences[i] = nodes[i].get();

        return sequences;
    }

}