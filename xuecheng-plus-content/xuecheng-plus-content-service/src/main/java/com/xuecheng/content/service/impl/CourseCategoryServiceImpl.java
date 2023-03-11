package com.xuecheng.content.service.impl;


import com.xuecheng.content.mapper.CourseCategoryMapper;

import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);

        HashMap<String,CourseCategoryTreeDto> map = new HashMap<>();

        List<CourseCategoryTreeDto> categoryTreeDtoList = new ArrayList<>();

        courseCategoryTreeDtos.stream().forEach(item->{
            map.put(item.getId(),item);

            if(item.getParentid().equals(id)){
                categoryTreeDtoList.add(item);
            }

            CourseCategoryTreeDto courseCategoryTreeDto = map.get(item.getParentid());
            if(courseCategoryTreeDto != null){
                if(courseCategoryTreeDto.getChildrenTreeNodes() == null){
                    courseCategoryTreeDto.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                }
                courseCategoryTreeDto.getChildrenTreeNodes().add(item);
            }
        });
        return categoryTreeDtoList;
    }
}
