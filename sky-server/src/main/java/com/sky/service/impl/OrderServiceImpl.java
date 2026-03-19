package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * 订单提交
     *
     * @param ordersSubmitDTO 订单提交DTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {
        //处理业务逻辑异常
        Long userId = BaseContext.getCurrentId();
//        购物车是否为空
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.get(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new OrderBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

//        地址是否为空
//        AddressBook getById(Long id);
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new OrderBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
//        向orders添加一条数据

        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(orders.PENDING_PAYMENT);
        orders.setUserId(userId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(orders.UN_PAID);
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());

        orderMapper.insert(orders);
//        向order_detail添加多条数据

        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);

//        清空购物车
        shoppingCartMapper.deleteByUserId(userId);

//        返回VO
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();
        return orderSubmitVO;

    }

    /**
     * 历史订单查询
     *
     * @param page     当前页码
     * @param pageSize 每页记录数
     * @param status   订单状态（可选）
     * @return 分页结果，包含订单列表和总记录数
     */
    @Override
    public PageResult historyOrders(int page, int pageSize, String status) {
        // 1. 分页参数设置
        PageHelper.startPage(page, pageSize);

        // 2. 用 Orders 接收，因为 XML 里定义的是 Orders
        List<Orders> ordersList = orderMapper.getByUserIdAndStatus(BaseContext.getCurrentId(), status);

        // 3. 强转 Page 对象获取总数
        Page<Orders> p = (Page<Orders>) ordersList;
        long total = p.getTotal();

        // 4. 创建一个用于返回的 VO 集合
        List<OrderVO> orderVOList = new ArrayList<>();

        // 5. 属性拷贝与明细填充
        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders orders : ordersList) {
                OrderVO orderVO = new OrderVO();
                // 关键：将父类 Orders 的属性拷贝给子类 OrderVO
                BeanUtils.copyProperties(orders, orderVO);

                // 填充子类特有的订单明细
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());
                orderVO.setOrderDetailList(orderDetails);

                orderVOList.add(orderVO);
            }
        }

        // 6. 构造分页结果返回
        return new PageResult(total, orderVOList);
    }

    /**
     * 订单详情查询
     *
     * @param id
     * @return
     */
    @Override
    public OrderVO orderDetail(Long id) {
        OrderVO orderVO = new OrderVO();
        Orders orders = orderMapper.getById(id);
        BeanUtils.copyProperties(orders, orderVO);
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /**
     * 订单取消
     *
     * @param id
     */
    @Override
    public void cancel(Long id) {
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Integer Status = orders.getStatus();
        if (Status > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders o = new Orders();
        o.setId(id);
        o.setStatus(orders.CANCELLED);
        o.setCancelReason("用户取消");
        o.setCancelTime(LocalDateTime.now());
        orderMapper.update(o);

    }

    /**
     * 订单再来一单
     *
     * @param id
     */
    @Override
    public void repetition(Long id) {
        Orders orders = orderMapper.getById(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        for (OrderDetail orderDetail : orderDetailList) {
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setUserId(BaseContext.getCurrentId());
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    /**
     * 条件查询订单
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<OrderVO> page = orderMapper.conditionSearch(ordersPageQueryDTO);
        for (OrderVO orderVO : page) {
            List<OrderDetail> details = orderVO.getOrderDetailList();
            if (details != null && !details.isEmpty()) {
                String orderDishes = details.stream()
                        .map(OrderDetail::getName)
                        .collect(Collectors.joining(" ")); // 使用 joining 更简洁高效
                orderVO.setOrderDishes(orderDishes);
            }
        }
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 订单统计
     *
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        List<Orders> ordersList = orderMapper.getAllOrders();
        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders orders : ordersList) {
                if (orders.getStatus() == Orders.TO_BE_CONFIRMED) {
                    orderStatisticsVO.setToBeConfirmed(orderStatisticsVO.getToBeConfirmed() + 1);
                }
                if (orders.getStatus() == Orders.CONFIRMED) {
                    orderStatisticsVO.setConfirmed(orderStatisticsVO.getConfirmed() + 1);
                }
                if (orders.getStatus() == Orders.DELIVERY_IN_PROGRESS) {
                    orderStatisticsVO.setDeliveryInProgress(orderStatisticsVO.getDeliveryInProgress() + 1);
                }

            }
        }
        return orderStatisticsVO;
    }

    @Override
    public void confirm(Long id) {
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.CONFIRMED)
                .build();
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders order = orderMapper.getById(ordersRejectionDTO.getId());
        if (order.getStatus() == Orders.TO_BE_CONFIRMED) {
            Orders orders = Orders.builder()
                    .id(ordersRejectionDTO.getId())
                    .status(Orders.CANCELLED)
                    .rejectionReason(ordersRejectionDTO.getRejectionReason())
                    .cancelTime(LocalDateTime.now())
                    .build();
            if (order.getPayStatus() == Orders.PAID) {
                orders.setPayStatus(Orders.REFUND);
                //todo 退款逻辑
            }
            orderMapper.update(orders);
        } else {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
    }

    @Override
    public void cancelOrder(OrdersCancelDTO ordersCancelDTO) {
        Orders order = orderMapper.getById(ordersCancelDTO.getId());
        Orders orders = Orders.builder()
                .id(ordersCancelDTO.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersCancelDTO.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .build();
        if (order.getPayStatus() == Orders.PAID) {
            orders.setPayStatus(Orders.REFUND);
            //todo 退款逻辑
        }
        orderMapper.update(orders);
    }

    @Override
    public OrderPaymentVO paymentMock(OrdersPaymentDTO ordersPaymentDTO) {
        // 1. (可选) 这里可以根据业务逻辑修改数据库中订单的状态，例如改为“待接单”
        // 但通常支付接口只是返回签名，真正的状态修改在支付回调里。
        // 为了方便调试，你可以在这里直接调用你原本的支付成功逻辑：
        // orderService.paySuccess(ordersPaymentDTO.getOrderNumber());

        // 2. 构建符合接口文档要求的 Mock 数据
        OrderPaymentVO vo = OrderPaymentVO.builder()
                .nonceStr("mock_nonce_" + System.currentTimeMillis()) // 随机字符串
                .paySign("mock_sign_random_string")                 // 签名
                .timeStamp(String.valueOf(System.currentTimeMillis() / 1000)) // 时间戳
                .signType("MD5")                                    // 签名算法
                .packageStr("prepay_id=mock_123456789")             // 模拟 prepay_id
                .build();
        Orders orders = orderMapper.getByNumber(ordersPaymentDTO.getOrderNumber());

        if (orders != null) {
            // 2. 更新订单状态
            orders.setStatus(Orders.TO_BE_CONFIRMED); // 设置为待接单状态
            orders.setPayStatus(Orders.PAID);         // 设置为已支付
            orders.setCheckoutTime(LocalDateTime.now()); // 设置结账时间

            // 3. 调用更新方法
            orderMapper.update(orders);
        }
        return vo;
    }
}
