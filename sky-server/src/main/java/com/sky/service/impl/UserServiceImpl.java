package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    @Override
    public User login(UserLoginDTO userLoginDTO) {
        Map<String, String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", userLoginDTO.getCode());
        map.put("grant_type", "authorization_code");
        String json = HttpClientUtil.doGet("https://api.weixin.qq.com/sns/jscode2session", map);
//        User user = new User();
//        user.setOpenid(openId);
        JSONObject jsonObject = JSONObject.parseObject(json);
        String openId = jsonObject.getString("openid");

        if(openId==null){
            throw new RuntimeException("登录失败");
        }
        User user = userMapper.getByOpenid(openId);
        if(user==null){
            user = new User();
            user.setOpenid(openId);
            user.setCreateTime(LocalDateTime.now());
            //如果用户不存在，则创建新用户
            userMapper.insert(user);
        }

        return user;

    }
}