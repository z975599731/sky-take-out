package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("新增菜品接口")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        redisTemplate.delete("dish_" + dishDTO.getCategoryId());
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("菜品分页查询接口")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询：{}", dishPageQueryDTO);
        PageResult pageResult = dishService.page(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    @ApiOperation("菜品删除接口")
    public Result delete(@RequestParam List<Long> ids){
        log.info("菜品删除：{}", ids);
        dishService.delete(ids);
        Set set= redisTemplate.keys("dish_*");
        redisTemplate.delete(set);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品和口味")
    public Result<DishVO> getById(@PathVariable Long id)
    {
        DishVO dishVO=dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    @PutMapping
    @ApiOperation("修改菜品接口")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品：{}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        Set set= redisTemplate.keys("dish_*");
        redisTemplate.delete(set);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("菜品起售/停售接口")
    public Result updateStatus(@PathVariable Integer status, Long id){
        log.info("菜品起售/停售：{},{}", status, id);
        dishService.updateStatus(status, id);
        Set set= redisTemplate.keys("dish_*");
        redisTemplate.delete(set);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品接口")
    public Result<List<Dish>> getByCategoryId(Long categoryId){
        log.info("根据分类id查询菜品：{}", categoryId);
        List<Dish> dishList=dishService.getByCategoryId(categoryId);
        return Result.success( dishList);
    }

}
