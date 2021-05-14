package com.hpg.dao.user;

import com.hpg.dao.BaseDao;
import com.hpg.pojo.Role;
import com.hpg.pojo.User;
import com.mysql.cj.util.StringUtils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public  class UserDaoImpl implements UserDao {

    @Override
    //得到要登陆的用户
    public User getLoginUser(Connection connection, String userCode) throws Exception {
        //首先准备好三个对象
        //预编译对象
        PreparedStatement pstm = null;
        //返回集对象
        ResultSet rs = null;
        //用户对象
        User user = null;

        //判断数据库连接是否成功 成功才执行sql
        if(connection != null) {
            //通过userCode 执行相关的sql语句
            String sql = "select * from smbms_user where userCode=?";
            Object[] params = {userCode};

            //通过sql传进来，得到相应的结果集对象
                rs = BaseDao.execute(connection, pstm, rs, sql, params);
                if(rs.next()) {
                    //生成一个user用户，最后用于返回，其值是通过结果集去得到的
                    user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUserCode(rs.getString("userCode"));
                    user.setUserPassword(rs.getString("userPassword"));
                    user.setUserName(rs.getString("userName"));
                    user.setGender(rs.getInt("gender"));
                    user.setBirthday(rs.getDate("birthday"));
                    user.setPhone(rs.getString("phone"));
                    user.setAddress(rs.getString("address"));
                    user.setUserRole(rs.getInt("userRole"));
                    user.setCreatedBy(rs.getInt("createdBy"));
                    user.setCreationDate(rs.getTimestamp("creationDate"));
                    user.setModifyBy(rs.getInt("modifyBy"));
                    user.setModifyDate(rs.getTimestamp("modifyDate"));

                }
                //关闭连接
                BaseDao.closeResource(null, pstm, rs);

        }


        return user;
    }

    @Override
    //修改当前用户的密码
    public int updatePwd(Connection connection, int id, String password) throws SQLException {
        //预编译语句
        PreparedStatement pstm = null;
        //执行结果
        int execute = 0;
        if(connection != null) {
            //sql语句
            String sql = "update smbms_user set userPassword = ? where id = ?";
            //获取参数
            Object params[] = {password, id};
            //调用BaseDao去执行我们的语句
            execute = BaseDao.execute(connection, pstm, sql, params);
            BaseDao.closeResource(null, pstm, null);
        }
        return execute;
    }

    @Override
    //根据用户名/用户角色查询用户总数
    public int getUserCount(Connection connection,String username, int userRole) throws Exception {
        //预编译对象
        PreparedStatement pstm = null;
        //返回集对象
        ResultSet rs = null;
        //用户总数
        int count = 0;

        if(connection != null) {
            StringBuffer sql = new StringBuffer();
            sql.append("select count(1) as count from smbms_user u,smbms_role r where u.userRole = r.id");
            ArrayList<Object> list = new ArrayList<Object>();

            //假设前端传了 username 那么sql语句后面就得加个模糊查询：and u.userName like ?
            if(!StringUtils.isNullOrEmpty(username)) {
                //字符串拼接
                sql.append(" and u.userName like ?");
                list.add("%"+username+"%");
            }
            //如果传了角色
            if(userRole > 0) {
                sql.append(" and u.userRole = ?");
                list.add(userRole);//index 1;
            }

            //把list转换为数组（代表着参数）
            Object[] params = list.toArray();

            System.out.println("UserDaoImpl -> getUserCount:"+sql.toString());//输出最后完整的SQL语句

            //执行Sql
            rs = BaseDao.execute(connection, pstm, rs, sql.toString(), params);

            if(rs.next() ) {
                count = rs.getInt("count");//从结果集中获取数量
            }
            BaseDao.closeResource(null, pstm, rs);
        }
        return count;
    }

    //通过条件查询-userList
    @Override
    public List<User> getUserList(Connection connection, String userName,int userRole,int currentPageNo, int pageSize)throws Exception {
        PreparedStatement pstm = null;
        ResultSet rs = null;
        List<User> userList = new ArrayList<User>();
        if(connection != null){
            StringBuffer sql = new StringBuffer();
            sql.append("select u.*,r.roleName as userRoleName from smbms_user u,smbms_role r where u.userRole = r.id");
            List<Object> list = new ArrayList<Object>();
            if(!StringUtils.isNullOrEmpty(userName)){
                sql.append(" and u.userName like ?");
                list.add("%"+userName+"%");
            }
            if(userRole > 0){
                sql.append(" and u.userRole = ?");
                list.add(userRole);
            }
            //在数据库中，分页显示 limit startIndex，pageSize；总数
            //当前页  (当前页-1)*页面大小
            //0,5	1,0	 01234
            //5,5	5,0	 56789
            //10,5	10,0 10~
            sql.append(" order by creationDate DESC limit ?,?");
            currentPageNo = (currentPageNo-1)*pageSize;
            list.add(currentPageNo);
            list.add(pageSize);

            Object[] params = list.toArray();
            System.out.println("sql ----> " + sql.toString());

            rs = BaseDao.execute(connection, pstm, rs, sql.toString(), params);
            while(rs.next()){
                User _user = new User();
                _user.setId(rs.getInt("id"));
                _user.setUserCode(rs.getString("userCode"));
                _user.setUserName(rs.getString("userName"));
                _user.setGender(rs.getInt("gender"));
                _user.setBirthday(rs.getDate("birthday"));
                _user.setPhone(rs.getString("phone"));
                _user.setUserRole(rs.getInt("userRole"));
                _user.setUserRoleName(rs.getString("userRoleName"));
                userList.add(_user);
            }
            BaseDao.closeResource(null, pstm, rs);
        }
        return userList;
    }

    //增加用户信息
    @Override
    public int add(Connection connection, User user) throws Exception {
        PreparedStatement pstm = null;
        int updateRows = 0;
        if(null != connection){
            String sql = "insert into smbms_user (userCode,userName,userPassword," +
                    "userRole,gender,birthday,phone,address,creationDate,createdBy) " +
                    "values(?,?,?,?,?,?,?,?,?,?)";
            Object[] params = {user.getUserCode(),user.getUserName(),user.getUserPassword(),
                    user.getUserRole(),user.getGender(),user.getBirthday(),
                    user.getPhone(),user.getAddress(),user.getCreationDate(),user.getCreatedBy()};
            updateRows = BaseDao.execute(connection, pstm, sql, params);
            BaseDao.closeResource(null, pstm, null);
        }
        return updateRows;
    }

    //通过userId删除user
    @Override
    public int deleteUserById(Connection connection,Integer delId) throws Exception  {
        PreparedStatement pstm = null;
        int flag = 0;
        if(null != connection){
            String sql = "delete from smbms_user where id=?";
            Object[] params = {delId};
            flag = BaseDao.execute(connection, pstm, sql, params);
            BaseDao.closeResource(null, pstm, null);
        }
        return flag;
    }

    //修改用户信息
    @Override
    public int modify(Connection connection, User user) throws Exception {
        int flag = 0;
        PreparedStatement pstm = null;
        if(null != connection){
            String sql = "update smbms_user set userName=?,"+
                    "gender=?,birthday=?,phone=?,address=?,userRole=?,modifyBy=?,modifyDate=? where id = ? ";
            Object[] params = {user.getUserName(),user.getGender(),user.getBirthday(),
                    user.getPhone(),user.getAddress(),user.getUserRole(),user.getModifyBy(),
                    user.getModifyDate(),user.getId()};
            flag = BaseDao.execute(connection, pstm, sql, params);
            BaseDao.closeResource(null, pstm, null);
        }
        return flag;
    }

    //通过userId获取user
    @Override
    public User getUserById(Connection connection, String id) throws Exception {
        User user = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        if(null != connection){
            String sql = "select u.*,r.roleName as userRoleName from smbms_user u,smbms_role r where u.id=? and u.userRole = r.id";
            Object[] params = {id};
            rs = BaseDao.execute(connection, pstm, rs, sql, params);
            if(rs.next()){
                user = new User();
                user.setId(rs.getInt("id"));
                user.setUserCode(rs.getString("userCode"));
                user.setUserName(rs.getString("userName"));
                user.setUserPassword(rs.getString("userPassword"));
                user.setGender(rs.getInt("gender"));
                user.setBirthday(rs.getDate("birthday"));
                user.setPhone(rs.getString("phone"));
                user.setAddress(rs.getString("address"));
                user.setUserRole(rs.getInt("userRole"));
                user.setCreatedBy(rs.getInt("createdBy"));
                user.setCreationDate(rs.getTimestamp("creationDate"));
                user.setModifyBy(rs.getInt("modifyBy"));
                user.setModifyDate(rs.getTimestamp("modifyDate"));
                user.setUserRoleName(rs.getString("userRoleName"));
            }
            BaseDao.closeResource(null, pstm, rs);
        }
        return user;
    }



}
