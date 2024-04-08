package com.example.etcd;

import io.etcd.jetcd.kv.DeleteResponse;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.kv.PutResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/etcd")
public class EtcdController {

    private final EtcdService etcdService;

    public EtcdController(EtcdService etcdService) {
        this.etcdService = etcdService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !Objects.equals(file.getContentType(), "text/csv")) {
            return ResponseEntity.badRequest().body("Invalid file or file type.");
        }
        try {
            etcdService.saveCsv(file.getInputStream());
            return ResponseEntity.ok().body("File uploaded and data saved successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving data: " + e.getMessage());
        }
    }

    @PostMapping("/put")
    public CompletableFuture<PutResponse> put(@RequestParam String key, @RequestParam String value) {
        return etcdService.put(key, value);
    }

    @GetMapping("/get")
    public CompletableFuture<GetResponse> get(@RequestParam String key) throws InterruptedException {
        return etcdService.get(key);
    }

    @DeleteMapping("/delete")
    public CompletableFuture<DeleteResponse> delete(@RequestParam String key) {
        return etcdService.delete(key);
    }

    @PostMapping("/delete-keys")
    public ResponseEntity<?> deleteKeys(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !Objects.equals(file.getContentType(), "text/csv")) {
            return ResponseEntity.badRequest().body("Invalid or empty file.");
        }

        try {
            CompletableFuture<Void> deleteFuture = etcdService.deleteKeysFromCsv(file.getInputStream());
            deleteFuture.join();  // Wait for all deletions to complete
            return ResponseEntity.ok().body("Keys deletion initiated.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during keys deletion: " + e.getMessage());
        }
    }
}
