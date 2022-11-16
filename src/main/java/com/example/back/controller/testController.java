package com.example.back.controller;

import com.example.back.Domain.Dto.LoginRequest;
import com.example.back.Domain.Dto.ResponseDto;
import com.example.back.Domain.Dto.oneDto;
import com.example.back.Domain.Dto.testDto;
import com.example.back.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class testController {
    private final UserService userService;


    @PostMapping("/test/loc")
    public testDto testLoc(@RequestBody testDto dto) {
        log.info(dto.getLatitude()+"!!!!!!!!!!!!!!!!!!!!!!!!!!");
        log.info(dto.getLongitude()+"!!!!!!!!!!!!!!!!!!!!!");
        return dto;
        // 넘어온 String형태의 위도 경도를 그대로 반환한다.
    }

    @PostMapping("/test/loc2")
    public List<oneDto> testLoc2(@RequestBody List<oneDto> list) {
        for (oneDto oneDto : list) {
            log.info("test2"+oneDto.getLatitude()+"!!!!!!!!!!!!!!!!!!!!!!!");
        }
        return list;
        // 넘어온 String형태의 위도 경도를 그대로 반환한다.
    }


}
