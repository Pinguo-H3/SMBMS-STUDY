package com.hpg.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

//操作数据库的公共类
public class BaseDao {

    private static String driver;
    private static String url;
    private static String username;
    private static String password;

    //静态代码块，类加载的时候就初始化啦
    static {

        Properties properties = new Properties();
        //通过类加载器读取对应资源 反射 把资源以流的形式读出来
//        Class<BaseDao> baseDaoClass = BaseDao.class; 获得class文件
//        ClassLoader classLoader = baseDaoClass.getClassLoader();
//        InputStream resourceAsStream = classLoader.getResourceAsStream("db.properties");
        InputStream is = BaseDao.class.getClassLoader().getResourceAsStream("db.properties");
        try {
            properties.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //为变量赋值
        driver = properties.getProperty("driver");
        url = properties.getProperty("url");
        username = properties.getProperty("username");
        password = properties.getProperty("password");

    }

    //获取数据库连接
    public static Connection getConnection() {
        Connection connection = null;
        try {
            //注册驱动
            Class.forName(driver);
            //连接
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    //编写查询公共类
    public static ResultSet execute(Connection connection,PreparedStatement preparedStatement,ResultSet resultSet,String sql,Object[] params) throws Exception {
        //预编译
        //预编译的sql不需要传参，直接执行即可 executeQuery()括号内不需要写sql
        preparedStatement = connection.prepareStatement(sql);

        for(int i = 0; i < params.length; i++) {
            //setObject 占位符从1开始 数组从0开始
            preparedStatement.setObject(i+1, params[i]);
        }
        //返回一个结果集参数
        resultSet = preparedStatement.executeQuery();
        return resultSet;
    }

    //编写增删改公共方法
    public static int execute(Connection connection, PreparedStatement preparedStatement, String sql, Object[] params) throws SQLException {
        int updateRows = 0;
        //预编译
        //预编译的sql不需要传参，直接执行即可
        preparedStatement = connection.prepareStatement(sql);

        for(int i = 0; i < params.length; i++) {
            //setObject 占位符从1开始 数组从0开始
            preparedStatement.setObject(i+1, params[i]);
        }
        //返回更新了多少行
        updateRows = preparedStatement.executeUpdate();
        return updateRows;
    }

    //释放资源，关闭连接
    public static boolean closeResource(Connection connection,PreparedStatement preparedStatement,ResultSet resultSet) {
        boolean flag = true;

        if(resultSet != null) {
            try {
                resultSet.close();
                resultSet = null;//GC垃圾回收
            } catch (SQLException e) {
                //如果关闭失败了
                e.printStackTrace();
                flag = false;
            }
        }
        if(preparedStatement != null){
            try {
                preparedStatement.close();
                preparedStatement = null;//GC垃圾回收
            } catch (SQLException e) {

                e.printStackTrace();
                flag = false;
            }
        }
        if(connection != null){
            try {
                connection.close();
                connection = null;//GC垃圾回收
            } catch (SQLException e) {
                e.printStackTrace();
                flag = false;
            }
        }
        return flag;

    }


}
