package ru.mipt.tasks.pools;

import ru.mipt.tasks.interfaces.ThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ScalableThreadPool implements ThreadPool {

    private final int minThreads;
    private final int maxThreads;
    private final List<Thread> threads;
    private final ConcurrentLinkedQueue<Runnable> taskQueue;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final AtomicInteger threadCount = new AtomicInteger();

    public ScalableThreadPool(int minThreads, int maxThreads) {
        this.minThreads = minThreads;
        this.maxThreads = maxThreads;
        this.threads = new ArrayList<>();
        this.taskQueue = new ConcurrentLinkedQueue<>();
        this.threadCount.set(minThreads);
    }

    @Override
    public void start() {
        for (int i = 0; i < minThreads; i++) {
            Thread thread = new CustomThread();
            thread.start();
            threads.add(thread);
        }
    }

    @Override
    public void execute(Runnable runnable) {
        taskQueue.offer(runnable);
        lock.lock();
        try {
            if (threadCount.get() < maxThreads) {
                Thread thread = new CustomThread();
                thread.start();
                threads.add(thread);
                threadCount.incrementAndGet();
            }
            condition.signal();
        } finally {
            lock.unlock();
        }
    }

    public int getActiveThreadCount() {
        return threadCount.get();
    }

    private class CustomThread extends Thread {
        public void run() {
            try {
                while (true) {
                    Runnable task;
                    lock.lock();
                    try {
                        while (taskQueue.isEmpty()) {
                            if (threads.size() > minThreads) {
                                threads.remove(this);
                                threadCount.decrementAndGet();
                                return;
                            }
                            condition.await();
                        }
                        task = taskQueue.poll();
                    } finally {
                        lock.unlock();
                    }
                    if (task != null) {
                        task.run();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
