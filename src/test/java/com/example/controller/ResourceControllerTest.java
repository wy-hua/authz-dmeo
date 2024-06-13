package com.example.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.model.DecodedAuthNHeader;
import com.example.model.Resource;
import com.example.model.UserRole;
import com.example.service.ResourceAclService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class ResourceControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private ResourceAclService resourceAclService;

  @BeforeEach
  public void setUp() {}

  @Test
  public void testGetResource_withValidAuthHeader_andValidResource_andValidAccess()
      throws Exception {
    String existingResources = UUID.randomUUID().toString();
    resourceAclService.addReadPermissions(
        List.of(Resource.builder().id(existingResources).build()), 1L);
    DecodedAuthNHeader userInfo = new DecodedAuthNHeader();
    userInfo.setUserId(1L);
    userInfo.setRole(UserRole.user);
    userInfo.setAccountName("test");
    String encodedAuthHeader =
        Base64.getEncoder().encodeToString(objectMapper.writeValueAsString(userInfo).getBytes());
    mockMvc
        .perform(
            get("/user/%s".formatted(existingResources))
                .header("X-Role-Info", encodedAuthHeader)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
  }

  @Test
  public void testGetResource_withInvalidAuthHeader() throws Exception {
    mockMvc
        .perform(
            get("/user/resource1")
                .header("X-Role-Info", "invalidAuthHeader")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testGetResource_withValidAuthHeader_andNonExistingResource() throws Exception {
    DecodedAuthNHeader userInfo = new DecodedAuthNHeader();
    userInfo.setUserId(1L);
    userInfo.setRole(UserRole.user);
    userInfo.setAccountName("test");
    String encodedAuthHeader =
        Base64.getEncoder().encodeToString(objectMapper.writeValueAsString(userInfo).getBytes());

    mockMvc
        .perform(
            get("/user/nonExistingResource")
                .header("X-Role-Info", encodedAuthHeader)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testGetResource_withValidAuthHeader_andValidResource_andNoAccess() throws Exception {
    String resourceId = UUID.randomUUID().toString();
    resourceAclService.addReadPermissions(List.of(Resource.builder().id(resourceId).build()), 2L);

    DecodedAuthNHeader userInfo = new DecodedAuthNHeader();
    userInfo.setUserId(1L);
    userInfo.setRole(UserRole.user);
    userInfo.setAccountName("test");
    String encodedAuthHeader =
        Base64.getEncoder().encodeToString(objectMapper.writeValueAsString(userInfo).getBytes());

    mockMvc
        .perform(
            get("/user/%s".formatted(resourceId))
                .header("X-Role-Info", encodedAuthHeader)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  @Test
  public void testGetResource_withNoAuthHeader() throws Exception {
    mockMvc
        .perform(get("/user/resource1").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }
}
