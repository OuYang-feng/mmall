package com.mmall.service.imp;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;


@Service("iUserService")
public class UserServiceImp implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUserName(username);
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        // todo 密码登录MD5
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username,md5Password);
        if(user == null){
            return ServerResponse.createByErrorMessage("密码错误");
        }
        user.setPassword(org.apache.commons.lang3.StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功",user);

    }

    public ServerResponse<String> register(User user){

        ServerResponse validResponse = this.checkValid(user.getUsername(), com.mmall.common.Const.USERNAME);
        if (!validResponse.isSuccess()){
            return validResponse;
        }

        validResponse = this.checkValid(user.getEmail(), com.mmall.common.Const.EMAIL);

        if (!validResponse.isSuccess()){
            return validResponse;
        }
        user.setRole(com.mmall.common.Const.Role.ROLE_CUSTOMER);
        //MD5加密

        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        int resultCount = userMapper.insert(user);
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }


    public ServerResponse<String> checkValid(String str,String type){
        if (StringUtils.isBlank(type)){
            int resultCount = userMapper.checkUserName(str);
            if(resultCount > 0){
                return ServerResponse.createByErrorMessage("eamli已存在");
            }
        }else {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccess("校验成功");
    }

    private ServerResponse<User> getUserInfo(HttpSession session){
        User user = (User) session.getAttribute(Const.USERNAME);
        if (user !=null){
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMessage("用户未登录，无法获取用户信息");
    }
}
