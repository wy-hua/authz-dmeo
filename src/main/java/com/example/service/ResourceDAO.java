package com.example.service;

import com.example.model.Resource;
import java.util.Optional;

public interface ResourceDAO {

  Optional<Resource> getById(String id);
}
