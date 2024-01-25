package com.ocean.angel.tool.controller;

import com.ocean.angel.tool.annotation.ApiLimiting;
import com.ocean.angel.tool.common.ResultBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试 Controller
 */
@RestController
@RequestMapping("/test")
public class TestController {

    @ApiLimiting(apiRequestLimit = 5, apiIpLimit = 1)
    @GetMapping("/limited/resource")
    public ResultBean<?> limitedResource() {
        return ResultBean.success();
    }
}
