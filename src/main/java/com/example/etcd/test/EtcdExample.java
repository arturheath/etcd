package com.example.etcd.test;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.etcd.jetcd.kv.GetResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class EtcdExample {

    public static void main(String[] args) {
        // Endpoint of one of the etcd nodes in your cluster
        String etcdUrl = "http://172.19.0.2:2379,http://172.19.0.3:2379,http://172.19.0.4:2379";

        try (Client client = Client.builder().endpoints(etcdUrl.split(",")).build();) {
            KV kvClient = client.getKVClient();

            // Key and value to put in etcd
            String key = "mykey";
            String value = "myvalue";

            // Putting a key-value
            kvClient.put(ByteSequence.from(key.getBytes()), ByteSequence.from(value.getBytes())).get();
            System.out.println("Put [" + key + "] with value [" + value + "]");

            // Getting the value back by key
            CompletableFuture<GetResponse> getFuture = kvClient.get(ByteSequence.from(key.getBytes()));
            GetResponse response = getFuture.get(); // Blocking call to wait for the result
            String fetchedValue = response.getKvs().get(0).getValue().toString();
            System.out.println("Got value [" + fetchedValue + "] for key [" + key + "]");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
