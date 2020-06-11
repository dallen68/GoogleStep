package com.google.sps.servlets;

import com.google.gson.Gson;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.lang.Integer;
import java.util.List;
import java.io.PrintWriter;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      UserService userService = UserServiceFactory.getUserService();
    if (userService.isUserLoggedIn()) {
        String logoutUrl = userService.createLogoutURL("/index.html");
        String email = userService.getCurrentUser().getEmail();
        out.println("true," + logoutUrl + "," + email);
    } else {
        String loginUrl = userService.createLoginURL("/index.html");
        out.println("false," + loginUrl);
    }
  }
}
