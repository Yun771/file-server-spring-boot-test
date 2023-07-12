package com.yun.fileServer.services;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class StorageServiceImpl implements StorageService {

    private final Path rootFolder = Paths.get(System.getProperty("user.home") + File.separator + "uploads");

    @Override
    public String save(MultipartFile file, String id) throws Exception {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed load file");
            }

            Path dir = Paths.get(this.rootFolder + File.separator + id);

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
    public Resource load(String path, String id) throws Exception {
        try {
            Path dir = Paths.get(this.rootFolder + File.separator + id);
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
    public List<String> loadAll(String id) throws Exception {
        List<String> files = new ArrayList<>();
        Path dir = Paths.get(this.rootFolder + File.separator + id);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                if (!Files.isDirectory(path))
                    files.add(path.getFileName().toString());
            }
            return files;

        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public StreamingResponseBody zipping(String id) throws Exception {

        Path dir = Paths.get(this.rootFolder + File.separator + id);
        int BUFFER_SIZE = 1024;

        FileOutputStream fos = new FileOutputStream("dirCompressed.zip");
        StreamingResponseBody streamResponseBody = (out) ->  {

            List<String> paths;
            try {
                paths = this.loadAll(id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            final ZipOutputStream zipOutputStream = new ZipOutputStream(fos);
            ZipEntry zipEntry = null;
            InputStream inputStream = null;

            try {
                for (String path : paths) {
                    File file = new File(path);
                    zipEntry = new ZipEntry(file.getName());

                    inputStream = new FileInputStream(file);

                    zipOutputStream.putNextEntry(zipEntry);
                    byte[] bytes = new byte[BUFFER_SIZE];
                    int length;
                    while ((length = inputStream.read(bytes)) >= 0) {
                        zipOutputStream.write(bytes, 0, length);
                    }

                }
                // set zip size in response
            } catch (IOException e) {
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (zipOutputStream != null) {
                    zipOutputStream.close();
                }
            }

        };


        return streamResponseBody;
    }
}

