package com.xuecheng.content.service;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CoursePublish;

/**
 * @author beamshaha
 * @creat 2023-02-09
 */
public interface CourseBaseInfoService {

    public PageResult<CourseBase> queryCourseBaseList(Long companyId,PageParams pageParams, QueryCourseParamsDto queryCourseParamsDto);

    CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto);

    CourseBaseInfoDto getCourseBaseInfo(Long courseId);

    CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto);

    void deleteCourse(Long companyId, Long courseId);


}
