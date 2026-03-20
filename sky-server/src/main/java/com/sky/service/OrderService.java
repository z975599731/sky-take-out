package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    /**
     * 提交订单
     *
     * @param ordersSubmitDTO 订单提交DTO
     * @return 订单提交VO
     */
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    PageResult historyOrders(int page, int pageSize, String status);

    OrderVO orderDetail(Long id);

    void cancel(Long id);

    void repetition(Long id);

    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderStatisticsVO statistics();

    void confirm(Long id);

    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    void cancelOrder(OrdersCancelDTO ordersCancelDTO);

    OrderPaymentVO paymentMock(OrdersPaymentDTO ordersPaymentDTO);

    void delivery(Long id);

    void complete(Long id);

    void reminder(Long id);
}
