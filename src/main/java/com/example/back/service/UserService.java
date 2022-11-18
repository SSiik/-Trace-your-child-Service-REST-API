package com.example.back.service;


import com.example.back.Domain.Dto.*;
import com.example.back.Domain.Dto.crosswalkk.cross;
import com.example.back.Domain.Dto.gps.childReq;
import com.example.back.Domain.Dto.gps.locInfo;
import com.example.back.Domain.Dto.gps.parReq;
import com.example.back.Domain.Entity.user;
import com.example.back.JwtTokenProvider;
import com.example.back.repository.crosswalkJDbcRepository;
import com.example.back.repository.userRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final JwtTokenProvider jwtTokenProvider;
    private final userRepository userRepository;
    private final crosswalkJDbcRepository crosswalkJDbcRepository;

    @Transactional
    public void joinUser(signDto signDto) {  //회원가입
//        ValidateDuplicateMember(signDto.toEntity());
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        signDto.setPassword(passwordEncoder.encode(signDto.getPassword()));
        user user = signDto.toEntity();
        if(signDto.isIdx() == false){ //이러면 자녀 회원가입, 따로 user에 셋팅을 해준다.
            user parentUser = userRepository.findByPhoneNum(signDto.getParentPhoneNum());
            user.setParent(parentUser);
            parentUser.getChildren().add(user);
            user.setHouse(signDto.getHouse());
            user.setSchool(signDto.getSchool());
            user.setDuration(signDto.getDuration());
        }
        userRepository.save(user);
    }


    @Transactional
    public ResponseDto loginChk(String userId, String password) { //로그인시 확인합니다.
        Optional<user> byUserId = userRepository.findByUserId(userId);
        ResponseDto responseDto = new ResponseDto();
        if(byUserId.isPresent()){
            user user = byUserId.get();
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            if(passwordEncoder.matches(password,user.getPassword())){
                String token = jwtTokenProvider.createToken(user.getUserId());
                if(user.isIdx()){ //이게 true면 부모.
                    responseDto.setStatus(200);
                    responseDto.setToken(token);
                    responseDto.setIdx(user.isIdx()); responseDto.setUserName(user.getUserName());
                    responseDto.setUserId(user.getUserId());
                    List<user> children = user.getChildren();
                    for (user child : children) {
                        locationDto locationDto = new locationDto();
                        locationDto.setUserId(child.getUserId()); locationDto.setUserName(child.getUserName());
                        locationDto.setHouse(child.getHouse()); locationDto.setIdx(child.isIdx());
                        locationDto.setSchool(child.getSchool()); locationDto.setDuration(child.getDuration());
                        responseDto.getChildrenInfo().add(locationDto);
                    }
                    return responseDto;
                }
                else{
                    childrenLoginDto childrenLoginDto = new childrenLoginDto();
                    responseDto.setStatus(200);
                    responseDto.setToken(token);
                    responseDto.setUserName(user.getUserName());  responseDto.setUserId(user.getUserId());
                    responseDto.setIdx(user.isIdx());
                    childrenLoginDto.setHouse(user.getHouse());
                    childrenLoginDto.setSchool(user.getSchool());
                    childrenLoginDto.setDuration(user.getDuration());
                    responseDto.setMyLocation(childrenLoginDto);
                    return responseDto;
                }
            }
            else{
                throw new RuntimeException("bad request");
            }
        }
        else{
            throw new RuntimeException("bad request");
        }
    }


    public childrenDto childrenRequest(childrenDto childrenDto) {
        Optional<user> byPhoneNumWithParent = userRepository.findByPhoneNumWithParent(childrenDto.getPhoneNum());
        if(byPhoneNumWithParent.isPresent()){
            user user = byPhoneNumWithParent.get();
            childrenDto dto = new childrenDto();
            dto.setPhoneNum(user.getParent().getPhoneNum());
            dto.setLatitude(childrenDto.getLatitude());
            dto.setLongitude(childrenDto.getLongitude());
            return dto;
        }
        else{
            throw new RuntimeException("bad request");
        }
    }

    public List<cross> reqForCross(){
        List<cross> crosses = crosswalkJDbcRepository.selectCross();
        return crosses;
    }

    @Cacheable(value = "userLoc", key = "#parReq.userId")
    public locInfo getCache(parReq parReq) {
        log.info("There is no children location!!!!!!!!!!!!!!!!!!!!!");
        locInfo locInfo = new locInfo();
        locInfo.setLongitude(null); locInfo.setLongitude(null);
        return locInfo;
    }


    @CachePut(value = "userLoc", key = "#childReq.userId")
    public locInfo putCache(childReq childReq) {
        locInfo locInfo = new locInfo();
        locInfo.setLatitude(childReq.getLatitude());
        locInfo.setLongitude(childReq.getLongitude());
        return locInfo;
    }

    public boolean checkUserIdDuplicate(String userId) {
        return userRepository.existsByUserId(userId);
    }
}
