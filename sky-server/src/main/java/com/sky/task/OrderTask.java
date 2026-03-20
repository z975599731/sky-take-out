package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(cron = "0 * * * * ? ")
    public void cancelOverTimeOrders() {
        log.info("取消超时订单的定时任务执行了...");
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);

        List<Orders> list = orderMapper.getStatusAndTime(Orders.PENDING_PAYMENT, time);
        if (list != null && list.size() > 0) {
            for (Orders order : list) {
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason("订单超时未支付，自动取消");
                order.setCancelTime(LocalDateTime.now());
                orderMapper.update(order);
            }
        }

    }

    @Scheduled(cron = "0 0 1 * * ? ")
    public void DeliveryOrder() {
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);

        List<Orders> list = orderMapper.getStatusAndTime(Orders.DELIVERY_IN_PROGRESS, time);
        if (list != null && list.size() > 0) {
            for (Orders order : list) {
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            }
        }

    }
}
