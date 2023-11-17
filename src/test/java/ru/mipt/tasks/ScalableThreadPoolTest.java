package ru.mipt.tasks;

import org.junit.jupiter.api.Test;
import ru.mipt.tasks.pools.ScalableThreadPool;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScalableThreadPoolTest {

    @Test
    public void testTaskExecution() throws InterruptedException {
        ScalableThreadPool pool = new ScalableThreadPool(2, 4);
        pool.start();

        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < 5; i++) {
            pool.execute(counter::incrementAndGet);
        }

        Thread.sleep(2000);
        assertEquals(5, counter.get());
    }

    @Test
    public void testScalability() throws InterruptedException {
        ScalableThreadPool pool = new ScalableThreadPool(2, 5);
        pool.start();

        AtomicInteger counter = new AtomicInteger(0);
        for (int i = 0; i < 20; i++) {
            pool.execute(counter::incrementAndGet);
        }

        assertEquals(5, pool.getActiveThreadCount());

        Thread.sleep(2000);

        assertEquals(20, counter.get());
        assertEquals(2, pool.getActiveThreadCount());
    }
}

