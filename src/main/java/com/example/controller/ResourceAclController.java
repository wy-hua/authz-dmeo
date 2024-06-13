// src/main/java/com/example/managersystem/controller/AdminController.java

package com.example.controller;

import com.example.model.DecodedAuthNHeader;
import com.example.model.Resource;
import com.example.model.UserReadAccess;
import com.example.model.UserRole;
import com.example.service.ResourceAclService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
public class ResourceAclController {
  private final ObjectMapper objectMapper;
  private final ResourceAclService resourceAclService;

  public ResourceAclController(ObjectMapper objectMapper, ResourceAclService resourceAclService) {
    this.objectMapper = objectMapper;
    this.resourceAclService = resourceAclService;
  }

  @PostMapping("/admin/addUser")
  public ResponseEntity<String> addUser(
      @RequestBody UserReadAccess userReadAccess,
      @RequestHeader("X-Role-Info") String base64EncodedRoleInfo) {
    if (base64EncodedRoleInfo == null || base64EncodedRoleInfo.isEmpty()) {
      return ResponseEntity.badRequest().body("Header X-Role-Info is required");
    }
    String decodedString;
    try {
      decodedString = new String(Base64.getDecoder().decode(base64EncodedRoleInfo));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body("Header format isn't base64 encoded");
    }
    DecodedAuthNHeader userInfo;
    try {
      userInfo = objectMapper.readValue(decodedString, DecodedAuthNHeader.class);
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Invalid header format");
    }
    log.debug(userInfo.toString());
    log.debug(userReadAccess.toString());
    UserRole role = userInfo.getRole();
    if (!UserRole.admin.equals(role)) {
      return ResponseEntity.status(403)
          .body("Only admin users are allowed to add read permissions");
    }
    Set<Resource> resourceSet = new HashSet<>();
    for (String endpoint : userReadAccess.getEndpoints()) {
      resourceSet.add(Resource.builder().id(endpoint).build());
    }
    resourceAclService.addReadPermissions(resourceSet, userReadAccess.getUserId());

    return ResponseEntity.ok("User access added successfully");
  }
}
