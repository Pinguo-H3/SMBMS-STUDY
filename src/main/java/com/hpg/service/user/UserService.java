package com.hpg.service.user;

import com.hpg.pojo.User;

import java.util.List;

public interface UserService {
    //用户登录业务
    public User login(String userCode, String password);
    //用户修改密码业务（根据User的id去修改密码）
    public boolean updatePwd( int id, String password);
    //查询记录数
    public int getUserCount(String username, int userRole);
    //根据条件查询用户列表
    public List<User> getUserList(String queryUserName, int queryUserRole, int currentPageNo, int pageSize);

    //增加用户信息
    public boolean add(User user);

    //根据ID删除user
    public boolean deleteUserById(Integer delId);

    //修改用户信息
    public boolean modify(User user);

    //根据ID查找user
    public User getUserById(String id);




}
