package com.xuecheng;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CourseCategoryMapperTest {


    @Autowired
    CourseCategoryService courseCategoryService;

    @Autowired
    CourseCategoryMapper courseCategoryMapper;
    @Autowired
    TeachplanMapper teachplanMapper;

    @Test
    void  testTreeNodes(){
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes("1");
        System.out.println(courseCategoryTreeDtos);
    }

    @Test
    void test(){
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryService.queryTreeNodes("1");
        System.out.println(courseCategoryTreeDtos);
    }

    @Test
    void test1(){
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(117);
        System.out.println(teachplanDtos);
    }
}
