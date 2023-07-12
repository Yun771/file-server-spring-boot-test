package com.yun.fileServer.services;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class StorageServiceImpl implements StorageService {

    private final Path rootFolder = Paths.get(System.getProperty("user.home") + File.separator + "uploads");

    @Override
    public String save(MultipartFile file, String id) throws Exception {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed load file");
            }

            Path dir = Paths.get(this.rootFolder + File.separator + "pdf");

            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }


            Files.copy(file.getInputStream(), dir.resolve(file.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);

            return file.getOriginalFilename();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Resource load(String path) throws Exception {
        try {
            Path dir = Paths.get(this.rootFolder + File.separator + "pdf");
            Resource resource = new UrlResource(dir.resolve(path).toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not found or load file " + path);
            }

        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not found or load file " + path);
        }
    }

    @Override
    public List<String> save(List<MultipartFile> files, String id) throws Exception {
        List<String> paths = new ArrayList<>();

        for (MultipartFile file : files) {
            paths.add(save(file, id));
        }

        return paths;
    }

    @Override
    public Stream<Path> loadAll() throws Exception {
        return null;
    }
}
