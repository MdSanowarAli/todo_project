package com.proit.todoApi.util;


import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Common Function
 *
 */

public class CommonFunction {


    public static  String getReportPath(HttpServletRequest request,String path) {
    	  return request.getServletContext().getRealPath(path);
    	// return request.getSession().getServletContext().getRealPath(path);
    	 //path = this.getClass().getClassLoader().getResource("").getPath();
    }
    /**
     * 
     * @param filePath
     * @return
     */
    public static  String getResoucePath(String filePath) {
      Resource resource = new ClassPathResource(filePath);
  	  try {
		return resource.getFile().getAbsolutePath();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  	  return null;
   }
    
    
    /**
     * 
     * @param file
     * @return
     */
    public static  File getResouceFile(String filePath) {
      Resource resource = new ClassPathResource(filePath);
  	  try {
		return resource.getFile();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  	  return null;
   }
    
    /**
     * 
     * @param file
     * @return
     */

    
    /**
     * 
     * @param file
     * @return
     */
    public static  String getResouceFileBase64String(byte[] fileByte) {
  
    	return Base64.getEncoder().encodeToString(fileByte);
    }
    
}




