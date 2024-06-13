// src/main/java/com/example/managersystem/controller/UserController.java

package com.example.controller;

import com.example.model.DecodedAuthNHeader;
import com.example.model.Resource;
import com.example.model.UserRole;
import com.example.service.ResourceAclService;
import com.example.service.ResourceDAO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.NoSuchFileException;
import java.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class ResourceController {
  private final ObjectMapper objectMapper;

  private final ResourceDAO resourceDAO;

  private final ResourceAclService resourceAclService;

  public ResourceController(
      ObjectMapper objectMapper, ResourceDAO resourceDAO, ResourceAclService resourceAclService) {
    this.objectMapper = objectMapper;
    this.resourceDAO = resourceDAO;
    this.resourceAclService = resourceAclService;
  }

  @GetMapping("/user/{resource}")
  public ResponseEntity<?> getResource(
      @PathVariable(name = "resource") String resourceId,
      @RequestHeader("X-Role-Info") String base64EncodedAuthInfo) {
    if (base64EncodedAuthInfo == null || base64EncodedAuthInfo.isEmpty()) {
      return ResponseEntity.badRequest().body("Header X-Auth-Info is required");
    }
    String decodedString;
    try {
      decodedString = new String(Base64.getDecoder().decode(base64EncodedAuthInfo));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body("Header format isn't base64 encoded");
    }

    DecodedAuthNHeader userInfo;
    try {
      userInfo = objectMapper.readValue(decodedString, DecodedAuthNHeader.class);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Invalid header format");
    }

    Long userId = userInfo.getUserId();
    // Adding resource is earlier than updating ACL list
    if (userInfo.getRole().equals(UserRole.user)) {
      try {
        if (!resourceAclService.hasAccess(Resource.builder().id(resourceId).build(), userId)) {
          return ResponseEntity.status(403)
              .body("User does not have access to the resource: " + resourceId);
        }
      } catch (NoSuchFileException e) {
        return ResponseEntity.status(404).body("No such resource: " + resourceId);
      }
    }

    Resource fetchedResource = resourceDAO.getById(resourceId).orElse(null);
    if (fetchedResource == null) {
      return ResponseEntity.status(404).body("No such resource: " + resourceId);
    }

    return ResponseEntity.ok("Successfully accessed resource: " + resourceId);
  }
}
