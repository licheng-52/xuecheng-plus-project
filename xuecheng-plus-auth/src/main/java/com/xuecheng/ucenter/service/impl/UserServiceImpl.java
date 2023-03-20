package com.xuecheng.ucenter.service.impl;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import javafx.application.Application;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserDetailsService {



    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    private XcUserMapper xcUserMapper;

    @Autowired
    private XcMenuMapper xcMenuMapper;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        //根据名字拿到用户

        AuthParamsDto authParamsDto = null;

        try {
            authParamsDto = JSON.parseObject(s,AuthParamsDto.class);
        } catch (Exception e){
           throw  new RuntimeException("认证请求数据格式不对");
        }

        //认证方式
        String authType = authParamsDto.getAuthType();
        String beanName = authType + "_authservice";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);

        XcUserExt user = authService.execute(authParamsDto);
        return getUserPrincipal(user);
    }

    /**
     * @description 查询用户信息
     * @param user  用户id，主键
     * @return com.xuecheng.ucenter.model.po.XcUser 用户信息
     * @author Mr.M
     * @date 2022/9/29 12:19
     */
    private UserDetails getUserPrincipal(XcUserExt user) {

        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(user.getId());
        List<String> permissions = new ArrayList<>();
        if(xcMenus.size() <= 0){
            permissions.add("p1");
        } else {
            xcMenus.forEach(m ->{
                permissions.add(m.getCode());
            });
        }
        user.setPermissions(permissions);
        String[] authorities = permissions.toArray(new String[0]);
        String password = user.getPassword();
        user.setPassword(null);
        String userString = JSON.toJSONString(user);
        UserDetails userDetails = User.withUsername(userString).password(password).authorities(authorities).build();
        return userDetails;
    }
}
