package com.example.service;

import com.example.model.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class FileBasedResourceDAO implements ResourceDAO {

  private static final String RESOURCE_FILE_PREFIX = "src/main/resources/data/resource_";

  private String getResourceFilePath(String id) {
    return RESOURCE_FILE_PREFIX + id + ".txt";
  }

  @Override
  public Optional<Resource> getById(String id) {
    String resourceFilePath = getResourceFilePath(id);
    Path path = Paths.get(resourceFilePath);
    if (Files.exists(path)) {
      try {
        String content = Files.readString(path);
        // Deserialize resource from content, assuming content represents Resource data
        Resource resource = Resource.builder().build();
        return Optional.of(resource);
      } catch (IOException e) {
        // Handle IO error
        e.printStackTrace();
      }
    }
    return Optional.empty();
  }
}
