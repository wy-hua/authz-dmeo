package com.example.service;

import com.example.model.Resource;
import java.nio.file.NoSuchFileException;

public interface ResourceAclService {
  void addReadPermissions(Iterable<Resource> resources, Long userId);

  boolean hasAccess(Resource resource, Long userId) throws NoSuchFileException;
}
