package com.example.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.model.DecodedAuthNHeader;
import com.example.model.UserReadAccess;
import com.example.model.UserRole;
import com.example.service.ResourceAclService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class ResourceAclControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private ResourceAclService resourceAclService;

  private UserReadAccess userReadAccess;
  private DecodedAuthNHeader decodedAuthNHeader;

  @BeforeEach
  public void setUp() {
    userReadAccess = new UserReadAccess();
    userReadAccess.setUserId(1L);
    userReadAccess.setEndpoints(List.of("endpoint1", "endpoint2"));

    decodedAuthNHeader = new DecodedAuthNHeader();
    decodedAuthNHeader.setUserId(1L);
    decodedAuthNHeader.setRole(UserRole.admin);
    decodedAuthNHeader.setAccountName("admin");
  }

  @Test
  public void testAddUser_withValidAdminRole_shouldReturnOk() throws Exception {
    String encodedAuthHeader =
        Base64.getEncoder()
            .encodeToString(objectMapper.writeValueAsString(decodedAuthNHeader).getBytes());

    mockMvc
        .perform(
            post("/admin/addUser")
                .header("X-Role-Info", encodedAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userReadAccess)))
        .andExpect(status().isOk())
        .andExpect(content().string("User access added successfully"));
  }

  @Test
  public void testAddUser_withMissingHeader_shouldReturnBadRequest() throws Exception {
    mockMvc
        .perform(
            post("/admin/addUser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userReadAccess)))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void testAddUser_withInvalidBase64Header_shouldReturnBadRequest() throws Exception {
    mockMvc
        .perform(
            post("/admin/addUser")
                .header("X-Role-Info", "invalidBase64")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userReadAccess)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Header format isn't base64 encoded"));
  }

  @Test
  public void testAddUser_withInvalidJsonHeader_shouldReturnBadRequest() throws Exception {
    String invalidJson = Base64.getEncoder().encodeToString("invalidJson".getBytes());

    mockMvc
        .perform(
            post("/admin/addUser")
                .header("X-Role-Info", invalidJson)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userReadAccess)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Invalid header format"));
  }

  @Test
  public void testAddUser_withNonAdminRole_shouldReturnForbidden() throws Exception {
    decodedAuthNHeader.setRole(UserRole.user);
    String encodedAuthHeader =
        Base64.getEncoder()
            .encodeToString(objectMapper.writeValueAsString(decodedAuthNHeader).getBytes());

    mockMvc
        .perform(
            post("/admin/addUser")
                .header("X-Role-Info", encodedAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userReadAccess)))
        .andExpect(status().isForbidden())
        .andExpect(content().string("Only admin users are allowed to add read permissions"));
  }

  @Test
  public void testAddUser_concurrentCalls() throws Exception {
    int numberOfThreads = 5; // Number of concurrent threads
    ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
    String encodedAuthHeader =
        Base64.getEncoder()
            .encodeToString(objectMapper.writeValueAsString(decodedAuthNHeader).getBytes());
    List<String> randomResourceIds = new ArrayList<>();
    for (int i = 0; i < numberOfThreads * 100; i++) {
      executorService.submit(
          () -> {
            try {
              UserReadAccess randomUserReadAccess =
                  UserReadAccess.builder()
                      .userId(1l)
                      .endpoints(List.of(UUID.randomUUID().toString()))
                      .build();
              randomResourceIds.add(randomUserReadAccess.getEndpoints().get(0));
              String requestContent = objectMapper.writeValueAsString(randomUserReadAccess);

              mockMvc
                  .perform(
                      post("/admin/addUser")
                          .header("X-Role-Info", encodedAuthHeader)
                          .contentType(MediaType.APPLICATION_JSON)
                          .content(requestContent))
                  .andExpect(status().isOk())
                  .andExpect(content().string("User access added successfully"));
            } catch (Exception e) {
              e.printStackTrace();
            }
          });
    }
    executorService.shutdown();
    executorService.awaitTermination(1, TimeUnit.MINUTES);

    for (String resourceId : randomResourceIds) {
      DecodedAuthNHeader _userInfo = new DecodedAuthNHeader();
      _userInfo.setUserId(1L);
      _userInfo.setRole(UserRole.user);
      _userInfo.setAccountName("test");
      String _encodedAuthHeader =
          Base64.getEncoder().encodeToString(objectMapper.writeValueAsString(_userInfo).getBytes());
      mockMvc
          .perform(
              get("/user/%s".formatted(resourceId))
                  .header("X-Role-Info", encodedAuthHeader)
                  .contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk());
    }
  }
}
