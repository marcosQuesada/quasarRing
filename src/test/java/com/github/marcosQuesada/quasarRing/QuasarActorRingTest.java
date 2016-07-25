package com.github.marcosQuesada.quasarRing;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class QuasarActorRingTest {

    @Test
    public void createARingOfActorsAndIterateMessage() throws Exception {
        int ringSize = 10;
        int iterations = 10;

        QuasarActorRing qar = new QuasarActorRing(ringSize, iterations);
        // assert last iteration
        int[] sequences = qar.buildRing();
        for (int item :sequences){
            assertEquals(item, iterations);
        }
    }
}
