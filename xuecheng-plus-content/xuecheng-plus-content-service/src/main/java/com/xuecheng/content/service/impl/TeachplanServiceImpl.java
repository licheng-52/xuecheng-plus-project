package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.execption.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.BindTeachplanMediaDto;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
        //根据id获得信息数据SELECT * FROM teachplan WHERE courseId = 117 AND grade = 1  AND orderby < 1 ORDER BY orderby DESC LIMIT 1
        Teachplan teachplan = teachplanMapper.selectById(teachplanId);
        Long parentid = teachplan.getParentid();
        Integer grade = teachplan.getGrade();
        Long courseId = teachplan.getCourseId();
        Integer orderby = teachplan.getOrderby();


        //根据前端获取的信息判断是上移还是下移

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

    @Override
    public void associationMedia(BindTeachplanMediaDto bindTeachplanMediaDto) {
        //通过id查数据
        Long teachplanId = bindTeachplanMediaDto.getTeachplanId();

        Teachplan teachplan = teachplanMapper.selectById(teachplanId);

        if(teachplan == null){
            XueChengPlusException.cast("教学计划为空");
        }

        Integer grade = teachplan.getGrade();
        if(grade != 2){
            XueChengPlusException.cast("添加视频的级别只能为节");
        }
        Long courseId = teachplan.getCourseId();
        LambdaQueryWrapper<TeachplanMedia> wrapper = new LambdaQueryWrapper<TeachplanMedia>().eq(TeachplanMedia::getCourseId,courseId);
        teachplanMediaMapper.delete(wrapper);


        TeachplanMedia teachplanMedia = new TeachplanMedia();
        teachplanMedia.setCourseId(courseId);
        teachplanMedia.setMediaId(bindTeachplanMediaDto.getMediaId());
        teachplanMedia.setTeachplanId(teachplanId);
        teachplanMedia.setMediaFilename(bindTeachplanMediaDto.getFileName());
        teachplanMedia.setCreateDate(LocalDateTime.now());
        teachplanMediaMapper.insert(teachplanMedia);



    }

    private void exchangeOrderby(Teachplan teachplan, Teachplan tmp) {
        if(tmp == null){
            XueChengPlusException.cast("课程信息不存在");
        }
        Integer tmpOrderby = tmp.getOrderby();
        Integer orderby = teachplan.getOrderby();
        tmp.setOrderby(tmpOrderby);
        teachplan.setOrderby(orderby);
        teachplanMapper.updateById(teachplan);
        teachplanMapper.updateById(tmp);



    }

    private int getTeachplanCount(Long courseId, Long parentId) {

        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getCourseId,courseId).eq(Teachplan::getParentid,parentId);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return count;
    }
}
