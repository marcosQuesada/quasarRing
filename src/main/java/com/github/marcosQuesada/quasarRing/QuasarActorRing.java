package com.github.marcosQuesada.quasarRing;

import co.paralleluniverse.actors.Actor;
import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.Channels;

/**
 * Created by marcos on 25/07/16.
 */
public class QuasarActorRing {
    protected int iterations;
    protected int totalNodesOnRing;
    protected ActorRef<Integer>[] register;
    protected Channel<Boolean> doneChan;

    public QuasarActorRing(int  nodesOnRing, int iterations) {
        this.totalNodesOnRing = nodesOnRing;
        this.iterations = iterations;
        this.register = new ActorRef[nodesOnRing];
        this.doneChan = Channels.newChannel(0);
    }

    protected static class NodeActor extends Actor<Integer, Integer> {

        protected ActorRef<Integer> nextNode = null;
        protected int id;
        protected int iterations;

        public NodeActor(int id, int iterations) {
            super(String.format("actor-%d", id), null);
            this.id = id;
            this.iterations = iterations;
        }

        @Override
        protected Integer doRun() throws InterruptedException, SuspendExecution {
            Integer rcv;
            for (; ; ) {
                rcv = receive();
                System.out.println("rcv " +String.format("%d",rcv) + " id "+String.format("%d",id));
                if (id == 0) {
                    rcv++; //iteration done!
                    System.out.println("Iteration Done"+ String.format("%d",rcv));
                }

                nextNode.send(rcv);

                if (rcv == iterations) {
                    System.out.println("Iterations achieved, exiting " + String.format("%d",id));
                    break;
                }

            }

            System.out.println("Exiting " + String.format("%d",id));
            return rcv;
        }

    }

    public int[] buildRing() throws Exception {
        // Initialize ring actors
        final NodeActor[] actors = new NodeActor[totalNodesOnRing];

        for (int i = 0; i < totalNodesOnRing; i++) {
            actors[i] = new NodeActor(i, iterations);
            register[i] = actors[i].spawn();
        }

        // Assing PIDs neighbour
        for (int i = 0; i < totalNodesOnRing; i++)
            actors[i].nextNode = register[(i+1) % totalNodesOnRing];

        // Initiate the ring.
        register[0].send(0);

        // Wait for actors to finish and collect the results.
        int[] sequences = new int[totalNodesOnRing];
        for (int i = 0; i < totalNodesOnRing; i++)
            sequences[i] = actors[i].get();
        return sequences;
    }
}
