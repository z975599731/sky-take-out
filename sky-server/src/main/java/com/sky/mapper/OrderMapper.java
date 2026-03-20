package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {
    @Insert("insert into orders (number, status, user_id, address_book_id, order_time, checkout_time, pay_method, pay_status, amount," +
            "remark, user_name, phone, address, consignee, cancel_reason, rejection_reason, cancel_time," +
            "estimated_delivery_time, delivery_status, delivery_time, pack_amount, tableware_number," +
            "tableware_status) values (#{number}, #{status}, #{userId}, #{addressBookId}, #{orderTime}, #{checkoutTime}," +
            "#{payMethod}, #{payStatus}, #{amount}, #{remark}, #{userName}, #{phone}, #{address}, #{consignee}," +
            "#{cancelReason}, #{rejectionReason}, #{cancelTime}, #{estimatedDeliveryTime}, #{deliveryStatus}," +
            "#{deliveryTime}, #{packAmount}, #{tablewareNumber}, #{tablewareStatus})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Orders orders);

//    Page<OrderVO> historyOrders(String status);

    List<Orders> getByUserIdAndStatus(Long userId, String status);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    void update(Orders o);

    Page<OrderVO> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders")
    List<Orders> getAllOrders();

    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> getStatusAndTime(@Param("status") Integer status, @Param("time") LocalDateTime time);
}