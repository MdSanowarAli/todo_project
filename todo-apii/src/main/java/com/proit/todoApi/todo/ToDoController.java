package com.proit.todoApi.todo;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proit.todoApi.util.Response;

/**
 * @author Md. Sanowar Ali
 *
 */

@RestController
@RequestMapping("/api/to-do")
public class ToDoController{
	
	@Autowired
	private ToDoService toDoService;
	
	@GetMapping("/grid-list")
	public Response gridList(HttpServletRequest request) {
		return toDoService.gridList(request);
	}
	
	@GetMapping("/list")
	public Response getAllList(@RequestBody(required = false) String reqObj) {
		return toDoService.getAllList(reqObj);
	}
	
	@GetMapping("/find-by-id")
	public Response findById(Long id) {
		return toDoService.findById(id);
	}
	
	@PostMapping("/create")
    public Response save(@RequestBody  String reqObj) {
        return toDoService.save(reqObj);
    }
	
	@PostMapping("/update")
    public Response update(@RequestBody  String reqObj) {
        return toDoService.update(reqObj);
    }
	
	@DeleteMapping("/delete")
	public Response delete(@RequestParam("id") long reqObj) {
		return toDoService.delete(reqObj);
	}
	
}
