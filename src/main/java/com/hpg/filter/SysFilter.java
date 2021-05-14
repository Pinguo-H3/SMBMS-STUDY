package com.hpg.filter;

import com.hpg.pojo.User;
import com.hpg.util.Constants;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SysFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException, ServletException {
       //为了获取Session
        HttpServletRequest request = (HttpServletRequest) req;
        //为了重定向
        HttpServletResponse response = (HttpServletResponse) resp;
        //过滤器，从Session中获取用户
        User user = (User) request.getSession().getAttribute(Constants.USER_SESSION);
        //如果被移除/注销/未登录
        if(user == null) {
            //System.out.println(request.getContextPath());
            response.sendRedirect(request.getContextPath()+"/error.jsp");
        } else {
            filterChain.doFilter(req, resp);
        }
    }

    @Override
    public void destroy() {

    }
}
