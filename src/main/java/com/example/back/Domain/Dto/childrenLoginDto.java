package com.example.back.Domain.Dto;

import com.example.back.Domain.Dto.locDto.house;
import com.example.back.Domain.Dto.locDto.school;
import lombok.Data;

@Data
public class childrenLoginDto {
    house house;
    school school;
    int duration;
}
