package ru.mipt.tasks.interfaces;

public interface ThreadPool {

    void start();
    void execute(Runnable runnable);
}

