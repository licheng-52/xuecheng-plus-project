package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
/**
 * @description 课程信息管理业务接口实现类
 * @author Mr.M
 * @date 2022/9/6 21:45
 * @version 1.0
 */
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {
    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;


    @Autowired
    private CourseCategoryMapper courseCategoryMapper;

    @Autowired
    private CourseMarketServiceImpl courseMarketService;

    @Autowired
    private CourseTeacherMapper courseTeacherMapper;

    @Autowired
    private TeachplanMapper teachplanMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams,
                                                      QueryCourseParamsDto queryCourseParamsDto) {
        //构建查询条件对象
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        //构建查询条件，根据课程名称查询
        queryWrapper.like(StringUtils.isNotEmpty(queryCourseParamsDto.getCourseName()),
                CourseBase::getName,queryCourseParamsDto.getCourseName());
        //构建查询条件，根据课程审核状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getAuditStatus()),
                CourseBase::getAuditStatus,queryCourseParamsDto.getAuditStatus());
        //构建查询条件，根据课程发布状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(queryCourseParamsDto.getPublishStatus()),
                CourseBase::getAuditStatus,queryCourseParamsDto.getPublishStatus());
        //分页对象
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(),
                pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<CourseBase> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<CourseBase> courseBasePageResult = new PageResult<>(list, total,
                pageParams.getPageNo(), pageParams.getPageSize());
        return courseBasePageResult;
    }

    @Override
    @Transactional
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {

        //合法性校验
        if (StringUtils.isBlank(dto.getName())) {
            throw new RuntimeException("课程名称为空");
        }
        if (StringUtils.isBlank(dto.getMt())) {
            throw new RuntimeException("课程分类为空");
        }
        if (StringUtils.isBlank(dto.getSt())) {
            throw new RuntimeException("课程分类为空");
        }
        if (StringUtils.isBlank(dto.getGrade())) {
            throw new RuntimeException("课程等级为空");
        }
        if (StringUtils.isBlank(dto.getTeachmode())) {
            throw new RuntimeException("教育模式为空");
        }
        if (StringUtils.isBlank(dto.getUsers())) {
            throw new RuntimeException("适应人群为空");
        }
        if (StringUtils.isBlank(dto.getCharge())) {
            throw new RuntimeException("收费规则为空");
        }

        CourseBase courseBaseNew = new CourseBase();
        BeanUtils.copyProperties(dto,courseBaseNew);
        courseBaseNew.setCreateDate(LocalDateTime.now());
        courseBaseNew.setAuditStatus("203001");
        courseBaseNew.setCompanyId(companyId);
        courseBaseNew.setStatus("202002");

        int insert = courseBaseMapper.insert(courseBaseNew);
        Long courseId = courseBaseNew.getId();

        //课程信营销信息表
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(dto,courseMarket);
        courseMarket.setId(courseId);

        String charge = dto.getCharge();

        if(charge.equals("202001")){
            Float price = dto.getPrice();
            if(price == null || price.floatValue() <= 0){
                throw new RuntimeException("课程设置了收费价格不能为空且必须大于0");
            }
        }

        //插入课程营销表
        int insert1 = courseMarketMapper.insert(courseMarket);
        if(insert1 <= 0 || insert <= 0){
            throw new RuntimeException("新增课程基本信息失败");
        }

        //返回课程信息列表
        return getCourseBaseInfo(courseId);
    }

    public CourseBaseInfoDto getCourseBaseInfo(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(courseBase == null){
            return null;
        }

        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if(courseMarket != null){
            BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        }
        CourseCategory courseCategorySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setStName(courseCategorySt.getName());


        CourseCategory courseCategoryMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryMt.getName());

        return courseBaseInfoDto;
    }

    @Override
    public CourseBaseInfoDto updateCourseBase(Long companyId, EditCourseDto editCourseDto) {

        Long courseId = editCourseDto.getId();

        CourseBase courseBase = courseBaseMapper.selectById(courseId);

        if(courseBase == null){
            XueChengPlusException.cast("课程信息不存在");
        }

        if(!companyId.equals(courseBase.getCompanyId())){
            XueChengPlusException.cast("本机构只可以修改自己机构的课程");
        }

        BeanUtils.copyProperties(editCourseDto,courseBase);

        courseBase.setChangeDate(LocalDateTime.now());
        int i = courseBaseMapper.updateById(courseBase);

        if(i<=0){
            XueChengPlusException.cast("修改课程基本信息失败");
        }

        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(editCourseDto,courseMarket);
        boolean isSuccess = this.saveCourseMarket(courseMarket);

        if (!isSuccess) {
            throw new XueChengPlusException("课程营销数据修改失败!");
        }

        CourseBaseInfoDto courseBaseInfo = getCourseBaseInfo(courseId);

        return courseBaseInfo;
    }

    @Override
    public void deleteCourse(Long companyId, Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if(companyId.equals(courseBase.getCompanyId())){
            XueChengPlusException.cast("本机构只可以删除自己机构的课程");
        }
        //删除课程教师信息
        LambdaQueryWrapper<CourseTeacher> teacherLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teacherLambdaQueryWrapper.eq(CourseTeacher::getCourseId,courseId);
        courseTeacherMapper.delete(teacherLambdaQueryWrapper);
        //删除课程计划
        LambdaQueryWrapper<Teachplan> teachplanLambdaQueryWrapper = new LambdaQueryWrapper<>();
        teachplanLambdaQueryWrapper.eq(Teachplan::getCourseId,courseId);
        teachplanMapper.delete(teachplanLambdaQueryWrapper);
        //删除营销计划
        courseMarketMapper.deleteById(courseId);
        //删除课程基本信息
        courseBaseMapper.deleteById(courseId);
    }

    private boolean saveCourseMarket(CourseMarket courseMarket) {

        String charge = courseMarket.getCharge();
        if(StringUtils.isBlank(charge)){
            XueChengPlusException.cast("请设置收费规则");
        }
        if(charge.equals("201001")){
            Float price = courseMarket.getPrice();
            if(price == null || price.floatValue()<=0){
                XueChengPlusException.cast("课程设置了收费价格不能为空且必须大于0");
            }
        }
        return courseMarketService.saveOrUpdate(courseMarket);
    }
}
