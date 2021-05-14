package com.hpg.servlet.user;


import com.hpg.pojo.User;
import com.hpg.service.user.UserServiceImpl;
import com.hpg.util.Constants;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class LoginServlet extends HttpServlet {

    //Servlet : 控制层，调用业务层代码
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("LoginServlet---start...");

        //从前端获取用户名和密码
        //里面的参数不是瞎写的 是根据前端的页面中那个地方对应过来的
        String userCode = req.getParameter("userCode");
        String userPassword = req.getParameter("userPassword");

        //将上述获得到的用户名和密码和数据库的进行对比 如何对比呢？调用业务层
        //new 一个登录业务对象
        UserServiceImpl userService = new UserServiceImpl();
        //通过这个对象 传入前面的到的用户名密码得到用户
        User user = userService.login(userCode, userPassword);
        //从数据库得到真实密码
        String userRealPassword = user.getUserPassword();
        System.out.println(userCode);
        System.out.println(userPassword);

        //若用户存在 且密码相等 视频里面少了这一步 要自己加
        if(user != null && userPassword.equals(userRealPassword)) {
            //将用户信息存放到Session中;
            req.getSession().setAttribute(Constants.USER_SESSION, user);
            //跳转到内部主页 使用重定向
            resp.sendRedirect("jsp/frame.jsp");
        } else {
            //如果不存在 就转发回登陆页面，并顺带提醒用户名/密码错误

            //这setAttribute方法作用是把 error 这个key赋予值"用户名或者密码不正确"
            req.setAttribute("error","用户名或者密码不正确");
            //分两步 1.获取了一个 RequestDispatcher 对象
            RequestDispatcher requestDispatcher = req.getRequestDispatcher("login.jsp");
            //2并转发到指定的 servlet
            requestDispatcher.forward(req, resp);
            //后续写顺手了 就可以放一起
            //req.getRequestDispatcher("login.jsp").forward(req,resp);

        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
