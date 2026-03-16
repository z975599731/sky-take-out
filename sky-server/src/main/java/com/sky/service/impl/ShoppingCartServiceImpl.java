package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {


    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车
     *
     * @param shoppingCartDTO 购物车数据传输对象
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        // 检查这里拿到的 ID 是不是 null

        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.get(shoppingCart);
        if (shoppingCartList != null && !shoppingCartList.isEmpty()) {
            // 如果购物车中已经存在该菜品或套餐，则数量加1
            ShoppingCart existingCart = shoppingCartList.get(0);
            existingCart.setNumber(existingCart.getNumber() + 1);
            shoppingCartMapper.update(existingCart);
        } else {
            Long dishId = shoppingCartDTO.getDishId();
            if (dishId != null) {
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setName(dish.getName());
            } else {
                Long setmealId = shoppingCartDTO.getSetmealId();

                shoppingCart.setAmount(setmealMapper.selectById(setmealId).getPrice());
                shoppingCart.setImage(setmealMapper.selectById(setmealId).getImage());
                shoppingCart.setName(setmealMapper.selectById(setmealId).getName());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());

            shoppingCartMapper.insert(shoppingCart);
        }

    }

    /**
     * 获取购物车列表
     *
     * @return
     */
    @Override
    public List<ShoppingCart> list() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        return shoppingCartMapper.get(shoppingCart);
    }

    /**
     * 清空购物车
     */
    @Override
    public void clean() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.get(shoppingCart);
        if (shoppingCartList != null && !shoppingCartList.isEmpty()) {
            ShoppingCart existingCart = shoppingCartList.get(0);
            if (existingCart.getNumber() > 1) {
                existingCart.setNumber(existingCart.getNumber() - 1);
                shoppingCartMapper.update(existingCart);
            } else {
                shoppingCartMapper.deleteById(existingCart.getId());
            }
        }
    }
}
