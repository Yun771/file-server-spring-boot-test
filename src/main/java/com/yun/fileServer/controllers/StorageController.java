package com.yun.fileServer.controllers;

import com.yun.fileServer.services.StorageServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("files")
public class StorageController {
    private final StorageServiceImpl storageService;

    public StorageController(StorageServiceImpl storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFiles(@RequestParam("files") List<MultipartFile> files, @RequestParam("id") String id) {
        try {
            var result = storageService.save(files, id);

            return ResponseEntity.ok(result.toString());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error");
        }

    }

    @GetMapping("/read/{id}/{fileName}")
    public ResponseEntity<Resource> getFile(@PathVariable("fileName") String filename, @PathVariable("id") String id) throws Exception {
        Resource file = storageService.load(filename, id);
        String contentType = Files.probeContentType(file.getFile().toPath());

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(file);
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<List<String>> filesFolder(@PathVariable("id") String id) throws Exception {
        return ResponseEntity.ok(storageService.loadAll(id));
    }
}
