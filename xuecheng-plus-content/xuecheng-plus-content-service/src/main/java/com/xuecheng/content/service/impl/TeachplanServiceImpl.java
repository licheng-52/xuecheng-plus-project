package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeachplanServiceImpl implements TeachplanService {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
        // 1.查询课程计划
        if (courseId == null || courseId <= 0){
            throw new XueChengPlusException("课程id非法!");
        }
        return teachplanMapper.selectTreeNodes(courseId);
    }

    @Override
    @Transactional
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {
        Long id = saveTeachplanDto.getId();

        if (id != null) {
            Teachplan teachplan = teachplanMapper.selectById(id);
            if (teachplan != null) {
                BeanUtils.copyProperties(saveTeachplanDto, teachplan);
                teachplanMapper.updateById(teachplan);
            }
        } else { // 新增
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            // 取出同父同级别的课程数量
            int count = getTeachplanCount(saveTeachplanDto.getCourseId(), saveTeachplanDto.getParentid());
            // 计算下默认顺序（新的课程计划的orderby）
            teachplan.setOrderby(count+1);

            teachplanMapper.insert(teachplan);
        }
    }

    @Override
    public void deleteTeachplan(Long teachplanId) {
        if(teachplanId == null){
            XueChengPlusException.cast("课程信息id为空");
        }
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);

        Integer grade = teachplan.getGrade();
        //判断章还是节
        if(grade == 1){
            LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Teachplan::getParentid,teachplanId);
            Integer count = teachplanMapper.selectCount(queryWrapper);
            if(count >= 0){
                XueChengPlusException.cast("章下面还有小节");
            }
            teachplanMapper.deleteById(teachplanId);
        } else {
            teachplanMapper.deleteById(teachplanId);
            LambdaQueryWrapper<TeachplanMedia> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TeachplanMedia::getTeachplanId,teachplanId);
            teachplanMediaMapper.delete(wrapper);
        }

    }

    @Transactional
    @Override
    public void orderByTeachplan(String moveType, Long teachplanId) {
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        Integer grade = teachplan.getGrade();
        Long parentid = teachplan.getParentid();
        Long courseId = teachplan.getCourseId();
        Integer orderby = teachplan.getOrderby();

        if("moveup".equals(moveType)){
            if(grade == 1){
                LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Teachplan::getCourseId,courseId)
                        .eq(Teachplan::getGrade,1)
                        .lt(Teachplan::getOrderby,orderby)
                        .orderByDesc(Teachplan::getOrderby)
                        .last("LIMIT 1");
                Teachplan tmp = teachplanMapper.selectOne(wrapper);
                exchangeOrderby(teachplan, tmp);
            } else if(grade == 2){
                LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Teachplan::getParentid,parentid)
                        .lt(Teachplan::getOrderby,orderby)
                        .orderByDesc(Teachplan::getOrderby)
                        .last("LIMIT 1");
                Teachplan tmp = teachplanMapper.selectOne(wrapper);
                exchangeOrderby(teachplan,tmp);
            }
        } else if("movedown".equals(moveType)){
            if(grade == 1){
                LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Teachplan::getCourseId,courseId)
                        .eq(Teachplan::getGrade,1)
                        .gt(Teachplan::getOrderby,orderby)
                        .orderByAsc(Teachplan::getOrderby)
                        .last("LIMIT 1");
                Teachplan tmp = teachplanMapper.selectOne(wrapper);
                exchangeOrderby(teachplan, tmp);
            } else if(grade == 2){
                LambdaQueryWrapper<Teachplan> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Teachplan::getParentid,parentid)
                        .gt(Teachplan::getOrderby,orderby)
                        .orderByAsc(Teachplan::getOrderby)
                        .last("LIMIT 1");
                Teachplan tmp = teachplanMapper.selectOne(wrapper);
                exchangeOrderby(teachplan,tmp);
            }
        }


    }

    private void exchangeOrderby(Teachplan teachplan, Teachplan tmp) {
        if(tmp == null){
            XueChengPlusException.cast("已经到头了，不能在移了");
        } else {
            Integer orderby = teachplan.getOrderby();
            Integer tmpOrderby = tmp.getOrderby();
            teachplan.setOrderby(tmpOrderby);
            tmp.setOrderby(orderby);
            teachplanMapper.updateById(tmp);
            teachplanMapper.updateById(teachplan);
        }
    }

    private int getTeachplanCount(Long courseId, Long parentId) {

        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,courseId).eq(Teachplan::getParentid,parentId);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return count;
    }
}
