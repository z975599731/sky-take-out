package com.sky.controller.user;

import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "订单相关接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 提交订单
     *
     * @param ordersSubmitDTO 订单提交DTO
     * @return 订单提交VO
     */
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        OrderSubmitVO orderSubmitVO = orderService.submit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    @GetMapping("/historyOrders")
    public Result<PageResult> historyOrders(int page, int pageSize, String status) {
        PageResult res = orderService.historyOrders(page, pageSize, status);
        return Result.success(res);
    }

    @GetMapping("/orderDetail/{id}")
    public Result<OrderVO> orderDetail(@PathVariable Long id) {
        OrderVO orderVO = orderService.orderDetail(id);
        return Result.success(orderVO);
    }

    @PutMapping("/cancel/{id}")
    public Result cancel(@PathVariable Long id) {
        orderService.cancel(id);
        return Result.success();
    }

    @PostMapping("/repetition/{id}")
    public Result repetition(@PathVariable Long id) {
        orderService.repetition(id);
        return Result.success();
    }

    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) {
        log.info("订单支付：{}", ordersPaymentDTO);
        // 调用模拟支付的方法
        OrderPaymentVO orderPaymentVO = orderService.paymentMock(ordersPaymentDTO);
        return Result.success(orderPaymentVO);
    }

    @GetMapping("/reminder/{id}")
    public Result reminder(@PathVariable Long id) {
        // 这里可以实现催单的逻辑，例如发送通知给商家等
        log.info("用户催单，订单ID：{}", id);
        orderService.reminder(id);
        return Result.success();
    }
}
