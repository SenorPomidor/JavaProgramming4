package ru.mipt.tasks.pools;

import ru.mipt.tasks.interfaces.ThreadPool;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class FixedThreadPool implements ThreadPool {

    private final int threadCount;
    private final ConcurrentLinkedQueue<Runnable> taskQueue;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    public FixedThreadPool(int threadCount) {
        this.threadCount = threadCount;
        this.taskQueue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void start() {
        for (int i = 0; i < threadCount; i++) {
            Thread thread = new CustomThread();
            thread.start();
        }
    }

    @Override
    public void execute(Runnable runnable) {
        taskQueue.offer(runnable);
        lock.lock();
        try {
            condition.signal();
        } finally {
            lock.unlock();
        }
    }

    private class CustomThread extends Thread {
        public void run() {
            while (true) {
                Runnable task;
                lock.lock();
                try {
                    while (taskQueue.isEmpty()) {
                        condition.await();
                    }
                    task = taskQueue.poll();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } finally {
                    lock.unlock();
                }

                if (task != null) {
                    try {
                        task.run();
                    } catch (RuntimeException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
}
