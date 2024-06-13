package com.example.model;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Resource {
  private String id;
  private Set<Long> userIdsAllowedToRead;
}
