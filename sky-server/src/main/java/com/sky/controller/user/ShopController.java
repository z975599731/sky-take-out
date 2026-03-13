package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Api(tags="商户相关接口")
@Slf4j
public class ShopController {
    @Autowired
    private RedisTemplate<String,String> redisTemplate;


    @GetMapping("/status")
    @ApiOperation("查询营业状态")
    public Result<Integer> getStatus() {
        ValueOperations<String,String> valueOperations = redisTemplate.opsForValue();
        Integer status = Integer.valueOf(valueOperations.get("shop_status"));
        log.info("{}",status);
        log.info("查询营业状态: {}", status==1 ? "营业中" : "打烊了");
        return Result.success(status);
    }

}
