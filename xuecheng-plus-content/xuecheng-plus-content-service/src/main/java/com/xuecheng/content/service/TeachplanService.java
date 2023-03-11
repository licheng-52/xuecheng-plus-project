package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;

import java.util.List;

public interface TeachplanService {

    public List<TeachplanDto> findTeachplanTree(Long courseId);

    void saveTeachplan(SaveTeachplanDto saveTeachplanDto);

    void deleteTeachplan(Long teachplanId);

    void orderByTeachplan(String moveType, Long teachplanId);
}
