package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;


@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    @Pointcut("execution( * com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }

    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("执行了自动填充的功能");
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autofill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autofill.value();
        log.info("操作类型：{}", operationType);
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }
        Object entity = args[0];
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        if (operationType == OperationType.INSERT) {
            try {
                //反射机制，获取实体类的setCreateTime方法
                val setCreateTimeMethod = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                //调用setCreateTime方法，传入当前时间
                setCreateTimeMethod.invoke(entity, now);
                //获取setUpdateTime方法
                val setUpdateTimeMethod = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                //调用setUpdateTime方法，传入当前时间
                setUpdateTimeMethod.invoke(entity, now);
                //获取setCreateUser方法
                val setCreateUserMethod = entity.getClass().getMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                //调用setCreateUser方法，传入当前用户id
                setCreateUserMethod.invoke(entity, currentId);
                //获取setUpdateUser方法
                val setUpdateUserMethod = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //调用setUpdateUser方法，传入当前用户id
                setUpdateUserMethod.invoke(entity, currentId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (operationType == OperationType.UPDATE) {
            try {
                //反射机制，获取实体类的setUpdateTime方法
                val setUpdateTimeMethod = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                //调用setUpdateTime方法，传入当前时间
                setUpdateTimeMethod.invoke(entity, now);
                //获取setUpdateUser方法
                val setUpdateUserMethod = entity.getClass().getMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //调用setUpdateUser方法，传入当前用户id
                setUpdateUserMethod.invoke(entity, currentId);
            } catch (Exception e) {
                e.printStackTrace();


            }
        }
    }
}