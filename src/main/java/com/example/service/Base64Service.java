// package com.example.service;
//
// import com.example.config.ObjectMapperConfig;
// import com.example.model.DecodedAuthNHeader;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Qualifier;
// import org.springframework.stereotype.Service;
//
// import java.util.Base64;
//
// @Service
// public class Base64Service {
//    @Autowired
//    private final  ObjectMapper mapper;
//
//    public Base64Service(ObjectMapper mapper) {
//        this.mapper = mapper;
//    }
//
//    public DecodedAuthNHeader decodeHeader(String encodedHeader) throws Exception {
//        String decodedString = decodeBase64(encodedHeader);
//        return mapper.readValue(decodedString, DecodedAuthNHeader.class);
//    }
//
//    public String encodeHeader(DecodedAuthNHeader header) throws Exception {
//        String jsonString = mapper.writeValueAsString(header);
//        return encodeBase64(jsonString);
//    }
//
//    private String decodeBase64(String encoded) {
//        byte[] decodedBytes = Base64.getDecoder().decode(encoded);
//        return new String(decodedBytes);
//    }
//
//    private String encodeBase64(String raw) {
//        byte[] encodedBytes = Base64.getEncoder().encode(raw.getBytes());
//        return new String(encodedBytes);
//    }
//
// }
