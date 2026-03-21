package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ReportMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private WorkspaceService workspaceService;

    /**
     * 营业额统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        TurnoverReportVO turnoverReportVO = new TurnoverReportVO();
        turnoverReportVO.setDateList(StringUtils.join(dateList, ","));
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Map<String, Object> map = new HashMap();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = reportMapper.sumByDateAndStatus(map);
            turnover = turnover == null ? 0 : turnover;
            turnoverList.add(turnover);
        }
        turnoverReportVO.setTurnoverList((StringUtils.join(turnoverList, ",")));
        return turnoverReportVO;
    }

    /**
     * 用户统计
     *
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        UserReportVO userReportVO = new UserReportVO();
        userReportVO.setDateList(StringUtils.join(dateList, ","));
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Map<String, Object> map = new HashMap();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            Integer newUser = userMapper.countByDate(map);
            newUser = newUser == null ? 0 : newUser;
            newUserList.add(newUser);
            Integer totalUser = userMapper.countBeforeDate(endTime);
            totalUser = totalUser == null ? 0 : totalUser;
            totalUserList.add(totalUser);
        }
        userReportVO.setNewUserList(StringUtils.join(newUserList, ","));
        userReportVO.setTotalUserList(StringUtils.join(totalUserList, ","));
        return userReportVO;

    }

    /**
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        OrderReportVO orderReportVO = new OrderReportVO();
        orderReportVO.setDateList(StringUtils.join(dateList, ","));
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            //查每天的
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Map<String, Object> map = new HashMap();
            map.put("beginTime", beginTime);
            map.put("endTime", endTime);
            Integer orderCount = orderMapper.countByDateAndStatus(map);
            orderCount = orderCount == null ? 0 : orderCount;
            orderCountList.add(orderCount);
            map.put("status", Orders.COMPLETED);
            Integer validOrderCount = orderMapper.countByDateAndStatus(map);
            validOrderCount = validOrderCount == null ? 0 : validOrderCount;
            validOrderCountList.add(validOrderCount);
        }
        orderReportVO.setOrderCountList(StringUtils.join(orderCountList, ","));
        orderReportVO.setValidOrderCountList(StringUtils.join(validOrderCountList, ","));
        int totalOrderCount = orderCountList.stream().mapToInt(Integer::intValue).sum();
        int validOrderCount = validOrderCountList.stream().mapToInt(Integer::intValue).sum();
        orderReportVO.setTotalOrderCount(totalOrderCount);
        orderReportVO.setValidOrderCount(validOrderCount);
        Double orderCompletionRate = 0.0;

// 1. 检查总数是否为 0，避免逻辑崩溃
        if (totalOrderCount != 0) {
            // 2. 强制转换其中一个操作数为 double，确保进行浮点除法
            orderCompletionRate = (double) validOrderCount / totalOrderCount;
        }

        orderReportVO.setOrderCompletionRate(orderCompletionRate);
        return orderReportVO;
    }

    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        Map<String, Object> map = new HashMap();
        map.put("beginTime", beginTime);
        map.put("endTime", endTime);
        map.put("status", Orders.COMPLETED);
        List<GoodsSalesDTO> goodsSalesDTOList = orderMapper.getTop10(map);
        List<String> nameList = new ArrayList<>();
        List<Integer> salesList = new ArrayList<>();
        for (GoodsSalesDTO goodsSalesDTO : goodsSalesDTOList) {
            nameList.add(goodsSalesDTO.getName());
            salesList.add(goodsSalesDTO.getNumber());
        }
        SalesTop10ReportVO salesTop10ReportVO = new SalesTop10ReportVO();
        salesTop10ReportVO.setNameList(StringUtils.join(nameList, ","));
        salesTop10ReportVO.setNumberList(StringUtils.join(salesList, ","));
        return salesTop10ReportVO;
    }

    @Override
    public void exportBusinessData(HttpServletResponse response) {
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);

        // 1. 获取 30 天汇总数据（用于填充表格上方的总览）
        BusinessDataVO totalData = workspaceService.getBusinessData(
                LocalDateTime.of(dateBegin, LocalTime.MIN),
                LocalDateTime.of(dateEnd, LocalTime.MAX));

        // 使用 try-with-resources 确保流关闭
        try (InputStream is = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
             XSSFWorkbook excel = new XSSFWorkbook(is)) {

            XSSFSheet sheet = excel.getSheetAt(0); // 获取第一个页签

            // --- A. 填充汇总数据 (Row 1, 3, 5) ---
            // 提取一个简单的赋值逻辑，防止代码太乱
            fillCell(sheet, 1, 1, "时间：" + dateBegin + "至" + dateEnd);

            fillCell(sheet, 3, 2, totalData.getTurnover());
            fillCell(sheet, 3, 4, totalData.getOrderCompletionRate());
            fillCell(sheet, 3, 7, totalData.getNewUsers());

            fillCell(sheet, 5, 2, totalData.getValidOrderCount());
            fillCell(sheet, 5, 4, totalData.getUnitPrice());

            // --- B. 填充 30 天明细数据 (Row 7 开始) ---
            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);

                // 重要：获取当天的独立数据并存入 dayData
                BusinessDataVO dayData = workspaceService.getBusinessData(
                        LocalDateTime.of(date, LocalTime.MIN),
                        LocalDateTime.of(date, LocalTime.MAX));

                int currentRow = 7 + i;
                fillCell(sheet, currentRow, 1, date.toString());
                fillCell(sheet, currentRow, 2, dayData.getTurnover());
                fillCell(sheet, currentRow, 3, dayData.getValidOrderCount());
                fillCell(sheet, currentRow, 4, dayData.getOrderCompletionRate());
                fillCell(sheet, currentRow, 5, dayData.getUnitPrice());
                fillCell(sheet, currentRow, 6, dayData.getNewUsers());
            }

            // --- C. 设置响应头并写出文件 ---
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 防止中文文件名乱码
            String fileName = "BusinessReport.xlsx";
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);
            outputStream.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 核心：万能单元格填充工具，彻底干掉 NullPointerException
     */
    private void fillCell(XSSFSheet sheet, int rowIdx, int cellIdx, Object value) {
        XSSFRow row = sheet.getRow(rowIdx);
        if (row == null) row = sheet.createRow(rowIdx);

        // 使用 CREATE_NULL_AS_BLANK 策略，如果 Cell 为空则自动创建
        XSSFCell cell = row.getCell(cellIdx, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }

}
