package com.example.etcd;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class EtcdService {

    private final KV kvClient;

    @Autowired
    public EtcdService(Client etcdClient) {
        this.kvClient = etcdClient.getKVClient();
    }

    @Timed(value = "etcd.put", description = "Time taken to put data into etcd")
    public CompletableFuture<PutResponse> put(String key, String value) {
        ByteSequence keyBS = ByteSequence.from(key.getBytes());
        ByteSequence valueBS = ByteSequence.from(value.getBytes());
        return kvClient.put(keyBS, valueBS);
    }

    public void saveCsv(InputStream csvInputStream) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvInputStream))) {
            Timer dbTimer = Metrics.timer("etcd.csv.upload");
            List<CompletableFuture<Void>> futureList = reader.lines()
                    .skip(1) // Skip CSV header if present
                    .map(line -> {
                        String[] kv = line.split(",");
                        if (kv.length == 2) {
                            String key = kv[0].trim();
                            String value = kv[1].trim();

                            // Create a new CompletableFuture to wrap the put call
                            CompletableFuture<Void> dbOperationFuture = new CompletableFuture<>();

                            // Record the async operation with Timer
                            dbTimer.record(() -> {
                                put(key, value)
                                        .thenAccept(putResponse -> dbOperationFuture.complete(null)) // Map PutResponse to Void
                                        .exceptionally(ex -> {
                                            dbOperationFuture.completeExceptionally(ex);
                                            return null;
                                        });
                                return dbOperationFuture;
                            });

                            return dbOperationFuture;
                        } else {
                            throw new IllegalArgumentException("Invalid CSV line format: " + line);
                        }
                    })
                    .toList();

            // Now we wait for all the futures to complete
            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
        }
    }

    @Timed(value = "etcd.get", description = "Time taken to get data from etcd")
    public CompletableFuture<GetResponse> get(String key) throws InterruptedException {
        ByteSequence keyBS = ByteSequence.from(key.getBytes());
        return kvClient.get(keyBS);
    }

    @Timed(value = "etcd.delete", description = "Time taken to delete data from etcd")
    public CompletableFuture<DeleteResponse> delete(String key) {
        ByteSequence keyBS = ByteSequence.from(key.getBytes());
        return kvClient.delete(keyBS);
    }

    public CompletableFuture<Void> deleteKeysFromCsv(InputStream csvInputStream) {
        Timer timer = Metrics.timer("etcd.csv.delete");
        AtomicInteger counter = new AtomicInteger();

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(csvInputStream))) {
                reader.lines().forEach(line -> {
                    String key = line.trim(); // Assuming each line in the CSV contains only the key
                    ByteSequence keyBS = ByteSequence.from(key.getBytes());

                    timer.record(() -> {
                        try {
                            kvClient.delete(keyBS).get();
                            counter.incrementAndGet();
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to delete key: " + key, e);
                        }
                    });
                });
            } catch (Exception e) {
                throw new RuntimeException("Failed to process the CSV file", e);
            }
        });

        future.thenRun(() -> System.out.println("Deleted " + counter.get() + " keys."));

        return future;
    }

    @Timed(value = "etcd.update", description = "Time taken to update data in etcd")
    public CompletableFuture<PutResponse> update(String key, String newValue) {
        return put(key, newValue);
    }
}
