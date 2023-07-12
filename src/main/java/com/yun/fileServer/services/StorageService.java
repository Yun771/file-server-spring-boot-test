package com.yun.fileServer.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface StorageService {

    String save(MultipartFile file, String id) throws Exception;

    Resource load(String path) throws Exception;

    List<String> save(List<MultipartFile> files, String id) throws Exception;

    Stream<Path> loadAll() throws Exception;

}
