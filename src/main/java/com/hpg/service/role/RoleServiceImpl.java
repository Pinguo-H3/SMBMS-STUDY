package com.hpg.service.role;

import com.hpg.dao.BaseDao;
import com.hpg.dao.role.RoleDao;
import com.hpg.dao.role.RoleDaoImpl;
import com.hpg.pojo.Role;
import org.junit.Test;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class RoleServiceImpl implements RoleService{
    //别忘了和之前一样 要引入相应的Dao哦
    private RoleDao roleDao;

    public RoleServiceImpl(){ roleDao = new RoleDaoImpl(); }

    @Override
    public List<Role> getRoleList() {
        Connection connection = null;
        List<Role> roleList = null;
        try {
            connection = BaseDao.getConnection();
            roleList = roleDao.getRoleList(connection);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            BaseDao.closeResource(connection, null, null);
        }
        return roleList;
    }

    @Test
    public void Test() {
        RoleServiceImpl roleService = new RoleServiceImpl();
        List<Role> roleList = roleService.getRoleList();
        for (Role role : roleList) {
            System.out.println(role.getRoleName());
        }
    }

}
