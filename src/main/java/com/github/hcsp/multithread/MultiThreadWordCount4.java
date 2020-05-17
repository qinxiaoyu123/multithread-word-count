package com.github.hcsp.multithread;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MultiThreadWordCount4 {
    // 使用threadNum个线程，并发统计文件中各单词的数量
    //使用 lock condition实现协同
    static Lock lock = new ReentrantLock();
    static Condition taskComplete = lock.newCondition();

    public static Map<String, Integer> count(int threadNum, List<File> files) {
        AtomicInteger counter = new AtomicInteger(threadNum);
        List<HashMap<String, Integer>> results = new CopyOnWriteArrayList<>();
        for (File file : files) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    HashMap<String, Integer> res = new HashMap<>();
                    try {
                        List<String> strings = Files.readAllLines(file.toPath());
                        for (String s : strings) {
                            String[] sSplit = s.split("\\s+");
                            for (String ss : sSplit) {
                                res.put(ss, res.getOrDefault(ss, 0) + 1);
                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    results.add(res);
                    counter.decrementAndGet();
                    lock.lock();
                    try{
                        taskComplete.signalAll();
                    }finally {
                        lock.unlock();
                    }
                }
            }).start();
        }
        lock.lock();
        try{
            while(counter.get() > 0){
                taskComplete.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
        HashMap<String, Integer> result = new HashMap<>();
        for (HashMap<String, Integer> tmp : results) {
            System.out.println(tmp.keySet());
            for (String s : tmp.keySet()) {
                result.put(s, result.getOrDefault(s, 0) + tmp.get(s));
            }
        }
        return result;
    }

}
