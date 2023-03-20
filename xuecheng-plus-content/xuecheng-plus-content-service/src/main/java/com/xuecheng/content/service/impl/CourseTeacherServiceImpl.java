package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {

    @Autowired
    CourseTeacherMapper courseTeacherMapper;

    @Override
    public List<CourseTeacher> getCourseTeacherList(Long courseId) {
        LambdaQueryWrapper<CourseTeacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseTeacher::getCourseId,courseId);
        return courseTeacherMapper.selectList(wrapper);
    }

    /**
     * 新增或者修改
     * @param courseTeacher
     * @return
     */
    @Override
    @Transactional
    public CourseTeacher saveCourseTeacher(CourseTeacher courseTeacher) {

        Long id = courseTeacher.getId();
        if(id == null){
            CourseTeacher teacher = new CourseTeacher();
            BeanUtils.copyProperties(courseTeacher,teacher);
            teacher.setCreateDate(LocalDateTime.now());
            int insert = courseTeacherMapper.insert(teacher);
            if(insert<=0){
                XueChengPlusException.cast("新增失败");
            }
            return courseTeacherMapper.selectById(teacher.getId());
        } else {
            CourseTeacher teacher = courseTeacherMapper.selectById(id);
            BeanUtils.copyProperties(courseTeacher,teacher);
            int i = courseTeacherMapper.updateById(teacher);
            if(i <= 0){
                XueChengPlusException.cast("修改失败");
            }
            return courseTeacherMapper.selectById(teacher.getId());
        }


    }



    @Override
    public void deleteCourseTeacher(Long courseId, Long teacherId) {
        LambdaQueryWrapper<CourseTeacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseTeacher::getCourseId,courseId);
        wrapper.eq(CourseTeacher::getId,teacherId);
        int delete = courseTeacherMapper.delete(wrapper);
        if(delete <= 0){
            XueChengPlusException.cast("删除教师失败下·");
        }
    }
}
