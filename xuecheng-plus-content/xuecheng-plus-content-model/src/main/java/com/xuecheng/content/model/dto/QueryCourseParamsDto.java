package com.xuecheng.content.model.dto;

import lombok.Data;
import lombok.ToString;

/**
 * @author beamshaha
 * @creat 2023-02-08
 */
@Data
@ToString
public class QueryCourseParamsDto {
    //审核状态
    private String auditStatus;
    //课程名称
    private String courseName;
    //发布状态
    private String publishStatus;

}
