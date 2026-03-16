package com.sky.mapper;


import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 获取购物车列表
     *
     * @param shoppingCart 购物车对象，包含查询条件
     * @return 购物车列表
     */
    List<ShoppingCart> get(ShoppingCart shoppingCart);

    /**
     * 更新购物车项
     *
     * @param existingCart
     */
    @Update("update shopping_cart set number = #{number}, amount = #{amount} where id = #{id}")
    void update(ShoppingCart existingCart);

    @Insert("Insert into shopping_cart (name, user_id, dish_id, setmeal_id, dish_flavor, number, amount, image, create_time) " +
            "values (#{name}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{image}, #{createTime})")
    void insert(ShoppingCart shoppingCart);

    @Delete("delete from shopping_cart where user_id = #{user_id}")
    void deleteByUserId(Long userId);

    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);
}

