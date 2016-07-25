package com.github.marcosQuesada.quasarRing;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.strands.channels.Channel;
import co.paralleluniverse.strands.channels.Channels;
import co.paralleluniverse.strands.channels.SelectAction;
import static co.paralleluniverse.strands.channels.Selector.receive;
import static co.paralleluniverse.strands.channels.Selector.select;
import co.paralleluniverse.strands.Strand;

public class QuasarFiberChannelRingTest {
    public QuasarFiberChannelRingTest() {
    }

    @Test
    public void sendMessageFromThreadToThread() throws Exception {
        final Channel<String> ch = Channels.newChannel(1);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(100);

                    ch.send("a message");
                } catch (InterruptedException | SuspendExecution ex) {
                    throw new AssertionError(ex);
                }
            }
        });


        thread.start();

        String m = ch.receive();

        assertEquals(m, "a message");

        thread.join();

    }

    // http://docs.paralleluniverse.co/quasar/#channels
    @Test
    public void senMessagesFromFibersUsingChannels() throws Exception{
        final Channel<Integer> chan1 = Channels.newChannel(0);
        final Channel<String> chan2 = Channels.newChannel(0);

        //Sender A
        new Fiber<Void>(() -> {
            for (int i = 0; i < 10; i++) {
                //Strand.sleep(100);
                chan1.send(i);
            }
            chan1.close();

        }).start();

        //Sender B
        new Fiber<Void>(() -> {
            for (int i = 0; i < 10; i++) {
                //Strand.sleep(100);
                chan2.send(Character.toString((char) ('a' + i)));
            }
            chan2.close();

        }).start();

        //GO channel consumer
        new Fiber<Void>(() -> {
            String msg = "";
            for (int i = 0; i < 20; i++) {
                SelectAction<Object> sa = select(receive(chan1),receive(chan2));
                switch (sa.index()) {
                    case 0:
                        msg = sa.message() != null ? "Got a number: " + (int) sa.message() : "ch1 closed";
                        System.out.println(msg);
                        break;
                    case 1:
                        msg = sa.message() != null ? "Got a string: " + (String) sa.message() : "ch1 closed";
                        System.out.println(msg);
                        break;
                }
            }

        }).start().join();

    }
}
