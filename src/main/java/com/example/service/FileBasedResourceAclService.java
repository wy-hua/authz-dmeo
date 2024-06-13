package com.example.service;

import com.example.model.Resource;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FileBasedResourceAclService implements ResourceAclService {

  private final Map<String, Lock> locks = new ConcurrentHashMap<>();
  private static final String RESOURCE_FILE_PREFIX = "src/main/resources/data/resource_";
  public static final String RESOURCE_ID_TO_ID_OF_USER_ALLOWED_TO_READ_KEY =
      "ids-of-users-allowed-to-read";

  private String getResourceFilePath(String resourceId) {
    return RESOURCE_FILE_PREFIX + resourceId + ".txt";
  }

  private Set<Long> readUserIdsFromFile(String resourceFilePath) throws IOException {
    if (!Files.exists(Paths.get(resourceFilePath))) {
      return new HashSet<>();
    }
    return Files.lines(Paths.get(resourceFilePath)).map(Long::valueOf).collect(Collectors.toSet());
  }

  private void writeUserIdsToFile(String resourceFilePath, Set<Long> userIds) throws IOException {
    Files.write(
        Paths.get(resourceFilePath),
        userIds.stream().map(String::valueOf).collect(Collectors.toList()),
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING);
  }

  /**
   * @param resources could be new ones
   * @param userId
   */
  @Override
  public void addReadPermissions(Iterable<Resource> resources, Long userId) {
    for (Resource resource : resources) {
      String resourceKey = resource.getId() + ":" + RESOURCE_ID_TO_ID_OF_USER_ALLOWED_TO_READ_KEY;
      Lock lock = locks.computeIfAbsent(resourceKey, k -> new ReentrantLock());
      lock.lock();
      try {
        String resourceFilePath = getResourceFilePath(resource.getId());
        Set<Long> userIds = readUserIdsFromFile(resourceFilePath);
        userIds.add(userId);
        writeUserIdsToFile(resourceFilePath, userIds);
      } catch (IOException e) {
        log.error("Error while accessing file for resource {}", resource.getId(), e);
      } finally {
        lock.unlock();
      }
    }
  }

  @Override
  public boolean hasAccess(Resource resource, Long userId) throws NoSuchFileException {
    String resourceKey = resource.getId() + ":" + RESOURCE_ID_TO_ID_OF_USER_ALLOWED_TO_READ_KEY;
    Lock lock = locks.computeIfAbsent(resourceKey, k -> new ReentrantLock());
    lock.lock();

    String resourceFilePath = getResourceFilePath(resource.getId());
    Path path = Paths.get(resourceFilePath);
    if (!Files.exists(path)) {
      throw new NoSuchFileException("");
    }
    try {
      Set<Long> userIds = readUserIdsFromFile(resourceFilePath);
      return userIds.contains(userId);
    } catch (IOException e) {
      log.error("Error while accessing file for resource {}", resource.getId(), e);
      return false;
    } finally {
      lock.unlock();
    }
  }
}
