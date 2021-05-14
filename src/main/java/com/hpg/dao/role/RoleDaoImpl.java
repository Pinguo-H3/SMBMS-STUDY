package com.hpg.dao.role;

import com.hpg.dao.BaseDao;
import com.hpg.pojo.Role;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class RoleDaoImpl implements RoleDao {
    @Override
    public List<Role> getRoleList(Connection connection) throws Exception {
        PreparedStatement pstm = null;
        ResultSet resultSet = null;
        //角色表
        ArrayList<Role> roleList = new ArrayList<Role>();

        if (connection!=null){
            String sql = "select * from smbms_role";
            Object[] params = {};
            resultSet = BaseDao.execute(connection, pstm, resultSet, sql, params);

            while (resultSet.next()){
                Role _role = new Role();
                //从数据库读一个个属性 然后装到role里 又把role存到一个role表里
                //角色id
                _role.setId(resultSet.getInt("id"));
                //角色号码 SMBMS_ADMIN SMBMS_MANAGER...
                _role.setRoleCode(resultSet.getString("roleCode"));
                //角色名
                _role.setRoleName(resultSet.getString("roleName"));
                //传到表里
                roleList.add(_role);
            }
            BaseDao.closeResource(null,pstm,resultSet);
        }
        return roleList;
    }
}
