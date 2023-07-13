package com.yun.fileServer.services;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class StorageServiceImpl implements StorageService {

    private final Path rootFolder = Paths.get(System.getProperty("user.home") + File.separator + "uploads");

    @Override
    public String save(MultipartFile file, String path) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed load file");
            }

            Path dir = Paths.get(this.rootFolder + File.separator + path);

            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }


            Files.copy(file.getInputStream(), dir.resolve(Objects.requireNonNull(file.getOriginalFilename())), StandardCopyOption.REPLACE_EXISTING);

            return file.getOriginalFilename();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public Resource load(String fileName, String path) {
        try {
            Path dir = Paths.get(this.rootFolder + File.separator + path);
            Resource resource = new UrlResource(dir.resolve(fileName).toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not found or load file " + fileName);
            }

        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not found or load file " + fileName);
        }
    }

    @Override
    public List<String> save(List<MultipartFile> files, String path) {
        List<String> paths = new ArrayList<>();

        for (MultipartFile file : files) {
            paths.add(save(file, path));
        }

        return paths;
    }

    @Override
    public List<String> loadAll(String id) throws Exception {
        List<String> files = new ArrayList<>();
        Path dir = Paths.get(this.rootFolder + File.separator + id);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                if (!Files.isDirectory(path))
                    files.add(path.getFileName().toString());
            }
            return files;

        }
    }

    @Override
    public Resource zipping(String path) throws Exception {
        Path dir = Paths.get(this.rootFolder + File.separator + path);

        File directory = new File(dir.toUri());
        File rootDirectory = new File(this.rootFolder.toUri());

        if (!directory.exists()) {
            throw new RuntimeException("Could not found or directory file ");

        }

        File zipFile = File.createTempFile(path, ".zip", rootDirectory);

        ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            ZipEntry zipEntry = new ZipEntry(file.getName());
            zipOutputStream.putNextEntry(zipEntry);

            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024];

            int bytesRead;

            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                zipOutputStream.write(buffer, 0, bytesRead);
            }

            fileInputStream.close();
        }

        zipOutputStream.close();

        Resource resource = new UrlResource(zipFile.toURI());

        if (!resource.exists() || !resource.isReadable()) {
            throw new RuntimeException("Could not found or load file ");
        }

        return resource;
    }
}

