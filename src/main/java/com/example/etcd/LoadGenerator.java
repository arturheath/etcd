package com.example.etcd;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Random;

@Slf4j
@Component
public class LoadGenerator {

    @Autowired
    private EtcdService etcdService;
    private final Random random = new Random();
    private final int initial = 1_000_000;
    private int lastCreatedId = initial;

    @PostConstruct
    public void initialize() {
        log.info("INITIAL: starting to insert {} records into etcd", initial);
        for (int i = 1; i <= initial; i++) {
            etcdService.put("k" + i, "v" + i);
        }
        log.info("INITIAL: {} records were inserted into etcd", initial);
    }

    @Scheduled(fixedRate = 10000, initialDelay = 180000)
    public void readOperationLoad() throws InterruptedException {
        int n = 10_000;
        log.info("GET: {} records to be read from etcd", n);
        for (int i = 1; i <= n; i++) {
            int keyIndex = random.nextInt(lastCreatedId) + 1;
            etcdService.get("k" + keyIndex);
        }
        log.info("GET: {} records were read from etcd", n);
    }

    @Scheduled(fixedRate = 30000, initialDelay = 180000)
    public void writeOperationLoad() {
        int n = 5_000;
        int l = lastCreatedId + n;
        int m = lastCreatedId;
        log.info("CREATE: {} records to be inserted into etcd", n);
        for (int i = m+1; i <= l; i++) {
            etcdService.put("k" + i, "v" + i);
            lastCreatedId++;
        }
        log.info("CREATE: {} records were inserted into etcd", n);
    }

    @Scheduled(fixedRate = 20000, initialDelay = 180000)
    public void updateOperationLoad() {
        int n = 2_000;
        log.info("UPDATE: {} records to be updated in etcd", n);
        for (int i = 1; i <= n; i++) {
            int keyIndex = random.nextInt(lastCreatedId) + 1;
            etcdService.update("k" + keyIndex, "newV" + keyIndex);
        }
        log.info("UPDATE: {} records were updated in etcd", n);
    }

    @Scheduled(fixedRate = 60000, initialDelay = 180000)
    public void deleteOperationLoad() {
        int n = 50_000;
        log.info("DELETE: {} records to be deleted from etcd", n);
        for (int i = 1; i <= n; i++) {
            int keyIndex = random.nextInt(lastCreatedId) + 1;
            etcdService.delete("k" + keyIndex);
        }
        log.info("DELETE: {} records were deleted from etcd", n);
    }
}
