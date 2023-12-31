package com.yun.fileServer.services;

import com.yun.fileServer.models.FileDetails;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface StorageService {

    FileDetails save(MultipartFile file, String path) throws Exception;

    Resource load(String fileName, String path) throws Exception;

    List<FileDetails> save(List<MultipartFile> files, String path) throws Exception;

    List<FileDetails> loadAll(String path) throws Exception;

    Resource zipping(String path) throws Exception;

    List<Map<String, Object>> deleteTempFilesZip() throws Exception;

}
