package com.github.hcsp.multithread;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class MultiThreadWordCount3 {
    //使用countDownLatch实现
    public static Map<String, Integer> count(int threadNum, List<File> files) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(threadNum);
        List<HashMap<String, Integer>> results = new ArrayList<>();
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
                    latch.countDown();
                }
            }).start();
        }
        latch.await();
        HashMap<String, Integer> result = new HashMap<>();
        for (HashMap<String, Integer> tmp : results) {
            for (String s : tmp.keySet()) {
                result.put(s, result.getOrDefault(s, 0) + tmp.get(s));
            }
        }
        return result;
    }
}
