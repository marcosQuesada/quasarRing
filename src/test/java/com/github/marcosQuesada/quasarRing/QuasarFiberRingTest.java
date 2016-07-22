package com.github.marcosQuesada.quasarRing;

import org.junit.Test;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class QuasarFiberRingTest {

    @Test
    public void testQuasarFiberRing() throws Exception {
        Logger log = LoggerFactory.getLogger(QuasarFiberRingTest.class);

        log.trace("Starting");
        int workerCount = 100;
        int ringSize = 100;
        QuasarFiberRing qfr = new QuasarFiberRing(workerCount, ringSize);
        int[] sequences = qfr.makeRing();

        final int offset = workerCount - ringSize % workerCount;
        for (int i = 0; i < workerCount; i++)
            try {
                assertEquals(
                        "sequence returned by Worker#" + i,
                        -((offset + i) % workerCount), sequences[i]);
            } catch (AssertionError ae) {
                log.trace("sequences[] = {}", Arrays.toString(sequences));
                throw ae;
            }
    }

}
