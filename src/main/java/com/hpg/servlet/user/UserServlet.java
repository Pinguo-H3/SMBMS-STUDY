package com.hpg.servlet.user;

import com.alibaba.fastjson.JSONArray;
import com.hpg.pojo.Role;
import com.hpg.pojo.User;
import com.hpg.service.role.RoleService;
import com.hpg.service.role.RoleServiceImpl;
import com.hpg.service.user.UserService;
import com.hpg.service.user.UserServiceImpl;
import com.hpg.util.Constants;
import com.hpg.util.PageSupport;
import com.mysql.cj.util.StringUtils;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//实现Servlet的复用
public class UserServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //从前端得方法 然后根据不同方法调用不同业务
        String method = req.getParameter("method");
        if(method.equals("savepwd") && method != null) {
            this.updatePwd(req, resp);
        }else if (method.equals("pwdmodify") && method != null) {
            this.pwdModify(req, resp);
        }else if (method.equals("query") && method != null) {
            this.query(req, resp);
        }else if (method.equals("add") && method != null) {
            this.add(req, resp);
        }else if(method.equals("deluser") && method != null) {
            this.delUser(req, resp);
        }else if(method.equals("modifyexe") && method != null) {
            this.modify(req, resp);
        }else if(method.equals("view") && method != null) {
            this.getUserById(req, resp, "userview.jsp");
        }else if(method.equals("getrolelist") && method != null) {
            this.getRoleList(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    //修改密码
    public void updatePwd(HttpServletRequest req, HttpServletResponse resp) {
        //从Session中取id
        Object o = req.getSession().getAttribute(Constants.USER_SESSION);
        //从req中拿密码，这个括号里的内容要与前端配对
        String newpassword = req.getParameter("newpassword");

//        System.out.println("UserServlet:"+newpassword);

        boolean flag = false;

//        System.out.println(o != null);
//        System.out.println(newpassword != null);
        //若用户存在 且 输入的密码不为空
        if(o != null && newpassword != null) {
            UserServiceImpl userService = new UserServiceImpl();
            flag = userService.updatePwd(((User) o).getId(), newpassword);
            //如果修改成功
            if(flag) {
                req.setAttribute("message","修改密码成功，请退出，使用新密码登录");
                //密码修改成功 移除Session
                req.getSession().removeAttribute(Constants.USER_SESSION);
            } else {
                req.setAttribute("message","修改密码失败 ");
            }
        } else {
            //新密码有问题
            req.setAttribute("message","新密码有问题");
        }
        try {
            req.getRequestDispatcher("pwdmodify.jsp").forward(req, resp);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //验证旧密码 session中有用户的密码 直接对比一下就好了
    public void pwdModify(HttpServletRequest req, HttpServletResponse resp) {
        //从Session中取id
        Object o = req.getSession().getAttribute(Constants.USER_SESSION);
        //从req中拿密码，此时是与js里的ajax相匹配 而不是前端的oldpassword不要搞错了
        String oldpassword = req.getParameter("oldpassword");

        //用万能的resultMap去存数据:此处的结果集用Map进行了代替
        HashMap<String, String> resultMap = new HashMap<String, String>();

        //session失效了/过期了
        if(o == null) {
            //"sessionerror"不是我自己瞎写的 是通过js中的data.result == "sessionerror"得到的
            resultMap.put("result", "sessionerror");
            //加入session没过期 但输入的密码为空的话
        } else if(StringUtils.isNullOrEmpty(oldpassword)) {
            resultMap.put("result", "error");
        } else {
            //得到session中用户密码
            String userPassword = ((User) o).getUserPassword();
            //如果老密码和session中密码一样 代表什么呢？
            //说明旧密码验证正确啦
            if(oldpassword.equals(userPassword)) {
                resultMap.put("result", "true");
                //否则旧密码输入不正确
            } else {
                resultMap.put("result", "false");
            }
        }

        try {
            //设置响应格式 以json方式响应
            resp.setContentType("application/json");
            PrintWriter writer = resp.getWriter();
            //JSONArray 阿里巴巴的JSON工具类 用于转换格式
            /*
            resultMap = ["result":"sessionerroe"] 格式是这样
            Json = {key,value} 格式是这样
            */
            writer.write(JSONArray.toJSONString(resultMap));
            //刷新
            writer.flush();
            //关闭
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    //重点
    public void query(HttpServletRequest req, HttpServletResponse resp) {
        //查询用户列表
        //从前端获取数据
        //首先拿三个我们需要地数据 名字、角色、当前页面
        String queryUserName = req.getParameter("queryname");
        //因为我不一定要选择userrole 所以暂时叫temp 注意是String类地 因此后面才需要用Integer.parseInt转成整型
        String temp = req.getParameter("queryUserRole");
        String pageIndex = req.getParameter("pageIndex");

        //页面用户人数
        int pageSize = 5;
        //当前用户页面
        int currentPageNo = 1;

        //获取用户列表
        UserServiceImpl userService = new UserServiceImpl();
        List<User> userList = null;
        List<Role> roleList = null;


            int queryUserRole = 0;
            //如果查询为空
            if(queryUserName == null) {
                queryUserName = "";
            }
            if(temp != null && !temp.equals("")) {
                queryUserRole = Integer.parseInt(temp);
            }
            if(pageIndex != null) {
                currentPageNo = Integer.parseInt(pageIndex);
            }

        //获取用户总数
        int totalCount = userService.getUserCount(queryUserName, queryUserRole);

        //总页数支持 需要用到PageSupport
        PageSupport pageSupport = new PageSupport();
        //设置当前是第几页
        pageSupport.setCurrentPageNo(currentPageNo);
        //页面容量
        pageSupport.setPageSize(pageSize);
        //总表数
        pageSupport.setTotalCount(totalCount);

        int totalPageCount = pageSupport.getTotalPageCount();
        //控制首页尾页 首页不能往前 尾页不能往后
        //小于第一页
        if(totalPageCount < 1) {
            currentPageNo = 1;
            //大于最后一页
        } else if(totalPageCount > totalPageCount) {
            currentPageNo = totalPageCount;
        }

        //获取用户列表展示
        //首先就要获取用户列表了
        //其实上面这么多东西都是为了准备数据 就是为了填下面这个函数各个参数 - -不容易
        userList = userService.getUserList(queryUserName, queryUserRole, currentPageNo, pageSize);
        //那么拿到这个列表了 我们终于可以给前端地userlist赋 这才是我们地最终目的 不然地前端地userlist空洞洞地 什么数据都没有
        req.setAttribute("userList",userList);

        //同理吧 给用户那一栏也赋值！
        RoleServiceImpl roleService = new RoleServiceImpl();
        roleList = roleService.getRoleList();
        //给用户列表赋值
        req.setAttribute("roleList",roleList);
        //给总用户数赋值
        req.setAttribute("totalCount",totalCount);
        //给当前页数赋值
        req.setAttribute("currentPageNo", currentPageNo);
        //给总页数赋值
        req.setAttribute("totalPageCount", totalPageCount);
        //给用户名赋值
        req.setAttribute("queryUserName", queryUserName);
        //给用户角色赋值
        req.setAttribute("queryUserRole", queryUserRole);

        //最后 返回前端
        try {
            req.getRequestDispatcher("userlist.jsp").forward(req, resp);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    //增加用户
    private void add(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("add()================");
        String userCode = request.getParameter("userCode");
        String userName = request.getParameter("userName");
        String userPassword = request.getParameter("userPassword");
        String gender = request.getParameter("gender");
        String birthday = request.getParameter("birthday");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String userRole = request.getParameter("userRole");

        User user = new User();
        user.setUserCode(userCode);
        user.setUserName(userName);
        user.setUserPassword(userPassword);
        user.setAddress(address);
        try {
            user.setBirthday(new SimpleDateFormat("yyyy-MM-dd").parse(birthday));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        user.setGender(Integer.valueOf(gender));
        user.setPhone(phone);
        user.setUserRole(Integer.valueOf(userRole));
        user.setCreationDate(new Date());
        user.setCreatedBy(((User)request.getSession().getAttribute(Constants.USER_SESSION)).getId());

        UserService userService = new UserServiceImpl();
        if(userService.add(user)){
            response.sendRedirect(request.getContextPath()+"/jsp/user.do?method=query");
        }else{
            request.getRequestDispatcher("useradd.jsp").forward(request, response);
        }

    }

    //删除用户
    private void delUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String id = request.getParameter("uid");
        Integer delId = 0;
        try{
            delId = Integer.parseInt(id);
        }catch (Exception e) {
            delId = 0;
        }
        HashMap<String, String> resultMap = new HashMap<String, String>();
        if(delId <= 0){
            resultMap.put("delResult", "notexist");
        }else{
            UserService userService = new UserServiceImpl();
            if(userService.deleteUserById(delId)){
                resultMap.put("delResult", "true");
            }else{
                resultMap.put("delResult", "false");
            }
        }

        //把resultMap转换成json对象输出
        response.setContentType("application/json");
        PrintWriter outPrintWriter = response.getWriter();
        outPrintWriter.write(JSONArray.toJSONString(resultMap));
        outPrintWriter.flush();
        outPrintWriter.close();
    }

    //修改用户
    private void modify(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String id = request.getParameter("uid");
        String userName = request.getParameter("userName");
        String gender = request.getParameter("gender");
        String birthday = request.getParameter("birthday");
        String phone = request.getParameter("phone");
        String address = request.getParameter("address");
        String userRole = request.getParameter("userRole");

        User user = new User();
        user.setId(Integer.valueOf(id));
        user.setUserName(userName);
        user.setGender(Integer.valueOf(gender));
        try {
            user.setBirthday(new SimpleDateFormat("yyyy-MM-dd").parse(birthday));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        user.setPhone(phone);
        user.setAddress(address);
        user.setUserRole(Integer.valueOf(userRole));
        user.setModifyBy(((User)request.getSession().getAttribute(Constants.USER_SESSION)).getId());
        user.setModifyDate(new Date());

        UserService userService = new UserServiceImpl();
        if(userService.modify(user)){
            response.sendRedirect(request.getContextPath()+"/jsp/user.do?method=query");
        }else{
            request.getRequestDispatcher("usermodify.jsp").forward(request, response);
        }

    }

    private void getPwdByUserId(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Object o = request.getSession().getAttribute(Constants.USER_SESSION);
        String oldpassword = request.getParameter("oldpassword");
        Map<String, String> resultMap = new HashMap<String, String>();

        if(null == o ){//session过期
            resultMap.put("result", "sessionerror");
        }else if(StringUtils.isNullOrEmpty(oldpassword)){//旧密码输入为空
            resultMap.put("result", "error");
        }else{
            String sessionPwd = ((User)o).getUserPassword();
            if(oldpassword.equals(sessionPwd)){
                resultMap.put("result", "true");
            }else{//旧密码输入不正确
                resultMap.put("result", "false");
            }
        }

        response.setContentType("application/json");
        PrintWriter outPrintWriter = response.getWriter();
        outPrintWriter.write(JSONArray.toJSONString(resultMap));
        outPrintWriter.flush();
        outPrintWriter.close();
    }

    //通过用户ID得到用户
    private void getUserById(HttpServletRequest request, HttpServletResponse response,String url)
            throws ServletException, IOException {
        String id = request.getParameter("uid");
        if(!StringUtils.isNullOrEmpty(id)){
            //调用后台方法得到user对象
            UserService userService = new UserServiceImpl();
            User user = userService.getUserById(id);
            request.setAttribute("user", user);
            request.getRequestDispatcher(url).forward(request, response);
        }

    }

    private void getRoleList(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Role> roleList = null;
        RoleService roleService = new RoleServiceImpl();
        roleList = ((RoleServiceImpl) roleService).getRoleList();
        //把roleList转换成json对象输出
        response.setContentType("application/json");
        PrintWriter outPrintWriter = response.getWriter();
        outPrintWriter.write(JSONArray.toJSONString(roleList));
        outPrintWriter.flush();
        outPrintWriter.close();
    }


}
