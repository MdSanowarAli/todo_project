package com.proit.todoApi.todo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.proit.todoApi.util.Response;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Md. Sanowar Ali
 *
 */

@Service
public class ToDoService {

	@Autowired
	private ToDoRepository toDoRepository;


	public Response gridList(HttpServletRequest request) {
		return toDoRepository.gridList(request);
	}
	
	public Response getAllList(String reqObj) {
		return toDoRepository.getAllList(reqObj);
	}
	
	public Response save(String reqObj) {
		return toDoRepository.save(reqObj);
	}
	
	public Response update(String reqObj) {
		return toDoRepository.update(reqObj);
	}
	
	public Response findById(Long id) {
		return toDoRepository.findById(id);

	}
	
	public Response delete(Long id) {
		return toDoRepository.delete(id);
	}
	
}
