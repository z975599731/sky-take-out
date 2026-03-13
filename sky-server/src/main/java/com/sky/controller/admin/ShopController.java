package com.sky.controller.admin;


import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags="商户相关接口")
@Slf4j
public class ShopController {

    @Autowired
    private RedisTemplate<String,String> redisTemplate;

    @PutMapping("/{status}")
    @ApiOperation("修改营业状态")
    public Result changeStatus(@PathVariable Integer  status) {
        log.info("修改营业状态: {}", status==1 ? "营业中" : "打烊了");
        ValueOperations <String,String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set("shop_status", status.toString() );
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("查询营业状态")
    public Result<Integer> getStatus() {
        ValueOperations <String,String> valueOperations = redisTemplate.opsForValue();
        Integer status = Integer.valueOf(valueOperations.get("shop_status"));
        log.info("{}",status);
        log.info("查询营业状态: {}", status==1 ? "营业中" : "打烊了");
        return Result.success(status);
    }
}
