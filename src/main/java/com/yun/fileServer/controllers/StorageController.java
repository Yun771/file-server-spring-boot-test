package com.yun.fileServer.controllers;

import com.yun.fileServer.models.FileDetails;
import com.yun.fileServer.services.StorageServiceImpl;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

    @GetMapping
    public String root() {
        return "Hola";
    }

    @PostMapping(value = "/upload")
    public ResponseEntity<FileDetails> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("path") String path) {

        return ResponseEntity.ok(storageService.save(file, path));
    }

    @PostMapping("/multi/upload")
    public ResponseEntity<List<FileDetails>> uploadFiles(@RequestParam("files") List<MultipartFile> files, @RequestParam("path") String path) {
        var result = storageService.save(files, path);

        return ResponseEntity.ok(result);

    }

    @GetMapping("/read/{path}/{fileName}")
    public ResponseEntity<Resource> getFile(@PathVariable("fileName") String filename, @PathVariable("path") String path) throws Exception {
        Resource file = storageService.load(filename, path);
        String contentType = Files.probeContentType(file.getFile().toPath());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData(
                "attachment",
                file.getFilename()
        );
        return ResponseEntity
                .ok()
                .headers(headers)
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(file);
    }

    @GetMapping("/read/{path}")
    public ResponseEntity<List<FileDetails>> filesFolder(@PathVariable("path") String path) throws Exception {
        return ResponseEntity.ok(storageService.loadAll(path));
    }

    @GetMapping("/download/{path}")
    public ResponseEntity<Resource> downloadFiles(@PathVariable("path") String path) throws Exception {
        Resource file = storageService.zipping(path);
        String contentType = Files.probeContentType(file.getFile().toPath());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData(
                "attachment",
                path + ".zip"
        );

        return ResponseEntity
                .ok()
                .headers(headers)
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(file);

    }

    // TODO: ELIMINAR CONTROLADOR E IMPLEMENTAR EL BORRADO DE ARCHIVOS MEDIANTE UNA TAREA CRON
    @DeleteMapping
    public ResponseEntity<?> deleteFiles() throws Exception {
        var data = this.storageService.deleteTempFilesZip();
        return ResponseEntity.ok(data);
    }
}
