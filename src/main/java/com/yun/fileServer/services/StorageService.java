package com.yun.fileServer.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface StorageService {

    String save(MultipartFile file, String id) throws Exception;

    Resource load(String path, String id) throws Exception;

    List<String> save(List<MultipartFile> files, String id) throws Exception;

    List<String> loadAll(String id) throws Exception;

    StreamingResponseBody zipping(String id) throws Exception;

}
