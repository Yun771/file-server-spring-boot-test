package com.yun.fileServer.services;

import com.yun.fileServer.models.FileDetails;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class StorageServiceImpl implements StorageService {

    private final Path rootFolder = Paths.get(System.getProperty("user.home") + File.separator + "uploads");

    @Override
    public FileDetails save(MultipartFile file, String path) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Failed load file");
            }

            Path dir = Paths.get(this.rootFolder + File.separator + path);

            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            file.getOriginalFilename();
            System.out.println(file.getOriginalFilename());



            Files.copy(file.getInputStream(), dir.resolve(Objects.requireNonNull(file.getOriginalFilename())), StandardCopyOption.REPLACE_EXISTING);

            FileDetails fileDetails = new FileDetails();

            fileDetails.setPath(path);
            fileDetails.setFullName(file.getOriginalFilename());
            fileDetails.setFileName(FilenameUtils.removeExtension(file.getOriginalFilename()));
            fileDetails.setContentType(file.getContentType());
            fileDetails.setExtension(FilenameUtils.getExtension(file.getOriginalFilename()));

            return fileDetails;
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
    public List<FileDetails> save(List<MultipartFile> files, String path) {
        List<FileDetails> paths = new ArrayList<>();

        for (MultipartFile file : files) {
            paths.add(save(file, path));
        }

        return paths;
    }

    @Override
    public List<FileDetails> loadAll(String path) throws Exception {
        List<FileDetails> fileDetails = new ArrayList<>();
        Path dir = Paths.get(this.rootFolder + File.separator + path);

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path filePath : stream) {
                if (!Files.isDirectory(filePath)){
                    FileDetails details = new FileDetails();
                    details.setFullName(filePath.getFileName().toString());
                    details.setPath(path);
                    details.setExtension(FilenameUtils.getExtension(filePath.getFileName().toString()));
                    details.setFileName(FilenameUtils.removeExtension(filePath.getFileName().toString()));
                    details.setContentType(Files.probeContentType(filePath));

                    fileDetails.add(details);

                }
            }
            return fileDetails;

        }
    }

    @Override
    public Resource zipping(String path) throws Exception {
        Path dir = Paths.get(this.rootFolder + File.separator + path);

        File directory = new File(dir.toUri());

        Path tempDirectory = Paths.get(this.rootFolder + File.separator + "tempFiles");

        if (!Files.exists(tempDirectory)) {
            Files.createDirectories(tempDirectory);
        }

        File rootDirectory = new File(tempDirectory.toUri());

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

    @Override
    public List<Map<String, Object>> deleteTempFilesZip() {
        try {
            Path tempDirectory = Paths.get(this.rootFolder + File.separator + "tempFiles");

            if (!Files.exists(tempDirectory)) return new ArrayList<>();


            List<Map<String, Object>> results = new ArrayList<>();
// ? Otro método para eliminar todos los archivos
//        FileUtils.cleanDirectory(tempDirectory.toFile());

            for (File file : Objects.requireNonNull(tempDirectory.toFile().listFiles())) {
                Map<String, Object> map = new HashMap<>();

                map.put("name", file.getName());

                Boolean deletedFile = file.delete();

                map.put("deleted", deletedFile);

                results.add(map);
            }

            return results;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

    }
}

