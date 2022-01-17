package com.sparta.springcore.controller;

import com.sparta.springcore.model.ApiUseTime;
import com.sparta.springcore.model.UserRoleEnum;
import com.sparta.springcore.repository.ApiUseTimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ApiUseTimeController {
    private final ApiUseTimeRepository apiUseTimeRepository;

    @Secured(UserRoleEnum.Authority.ADMIN)
    @GetMapping("api/use/time")
    public List<ApiUseTime> getAllApiUseTime(){
        return apiUseTimeRepository.findAll();
    }
}
