package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Mapper
public interface UserMapper {
    @Select("select * from user where openid = #{openId}")
    User getByOpenid(String openId);

    void insert(User user);

    Integer countByDate(Map<String, Object> map);

    Integer countBeforeDate(LocalDateTime endTime);
}
