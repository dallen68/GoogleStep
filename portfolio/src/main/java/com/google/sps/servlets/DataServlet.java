// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
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

/**Servlet that handles comments **/
@WebServlet("/comments")
public class DataServlet extends HttpServlet {
    
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    //Query the datastore for comments
    Query query = new Query("Comment").addSort("timestamp", SortDirection.DESCENDING);

    int count = Integer.parseInt(request.getParameter("count"));
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);
    
    String languageCode = request.getParameter("languageCode");
    //Iterate through query results and add entities to List
    List<String> comments = new ArrayList<>();
    int counter = 0;
    for (Entity entity : results.asIterable()) {
        if (counter < count) {
            String comment = (String) entity.getProperty("comment");
            // Translate comment to requested language
            Translate translate = TranslateOptions.getDefaultInstance().getService();
            Translation translation =
            translate.translate(comment, Translate.TranslateOption.targetLanguage(languageCode));

            String email = (String) entity.getProperty("email");
            comments.add(email + ":" + translation.getTranslatedText());
            counter++;
        }
      
    }
    
    response.setContentType("application/json");
    String json = new Gson().toJson(comments);
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
      String textComment = request.getParameter("comment");
      long timestamp = System.currentTimeMillis();
      UserService userService = UserServiceFactory.getUserService();
      String email = userService.getCurrentUser().getEmail();

      Entity commentEntity = new Entity("Comment");
      commentEntity.setProperty("comment", textComment);
      commentEntity.setProperty("timestamp", timestamp);
      commentEntity.setProperty("email", email);
      
      DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
      datastore.put(commentEntity);
      response.sendRedirect("index.html");
  }
}
