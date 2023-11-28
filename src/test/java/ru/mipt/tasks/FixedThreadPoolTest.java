package ru.mipt.tasks;

import org.junit.jupiter.api.Test;
import ru.mipt.tasks.pools.FixedThreadPool;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

public class FixedThreadPoolTest {

    @Test
    public void test() throws InterruptedException {
        FixedThreadPool pool = new FixedThreadPool(2);
        pool.start();

        AtomicInteger counter = new AtomicInteger(0);

        for (int i = 0; i < 5; i++) {
            pool.execute(counter::incrementAndGet);
        }

        Thread.sleep(1000);

        assertEquals(5, counter.get());
    }
}
