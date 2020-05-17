package com.github.hcsp.multithread;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class MultiThreadWordCount2 {
    //使用threadNum个线程，并发统计文件中各单词的数量
    //使用自定义线程池
    public static Map<String, Integer> count(int threadNum, List<File> files) throws ExecutionException, InterruptedException {
        ExecutorService threadPool = new ThreadPoolExecutor(threadNum,
                threadNum,
                0,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactory() {
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "thread");
                        return t;
                    }
                },
                new ThreadPoolExecutor.AbortPolicy());

        List<Future<Map<String, Integer>>> futures = new ArrayList<>();
        for (File file : files) {
            Future<Map<String, Integer>> future = threadPool.submit(new MultiThreadWordCount1.WordCount(file));
            futures.add(future);
        }
        HashMap<String, Integer> res = new HashMap<>();
        for (Future<Map<String, Integer>> future : futures) {
            HashMap<String, Integer> tmp = (HashMap<String, Integer>) future.get();
            for (String s : tmp.keySet()) {
                res.put(s, res.getOrDefault(s, 0) + tmp.get(s));
            }
        }
        return res;
    }

    static class WordCount implements Callable<Map<String, Integer>> {
        File file;

        WordCount(File file) {
            this.file = file;
        }

        @Override
        public Map<String, Integer> call() throws Exception {
            HashMap<String, Integer> res = new HashMap<>();
            List<String> strings = Files.readAllLines(file.toPath());
            for (String str : strings) {
                for (String st : str.split("\\s+")) {
                    res.put(st, res.getOrDefault(st, 0) + 1);
                }
            }
            return res;
        }
    }
}
