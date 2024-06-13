package com.example.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Base64;
import lombok.*;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class DecodedAuthNHeader {
  @NonNull private Long userId;
  @NonNull private String accountName;
  @NonNull private UserRole role;

  public static DecodedAuthNHeader fromBase64EncodedHeader(ObjectMapper objectMapper, String header)
      throws JsonProcessingException {
    // if header is null or empty throw exception
    String decodedString = Arrays.toString(Base64.getDecoder().decode(header));
    // if header is not as per the DecodedAuthNHeader.java structure, throw exception
    DecodedAuthNHeader userInfo = objectMapper.readValue(decodedString, DecodedAuthNHeader.class);
    return userInfo;
  }
}
