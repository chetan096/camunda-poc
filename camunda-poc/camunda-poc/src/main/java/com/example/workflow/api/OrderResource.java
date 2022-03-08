package com.example.workflow.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceWithVariablesDto;
import org.camunda.bpm.engine.rest.dto.runtime.StartProcessInstanceDto;
import org.camunda.bpm.engine.rest.dto.task.CompleteTaskDto;
import org.camunda.bpm.engine.rest.dto.task.TaskDto;
import org.camunda.bpm.engine.rest.dto.task.UserIdDto;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.workflow.dtos.OrderDO;
import com.example.workflow.dtos.PaymentDO;
import com.example.workflow.dtos.TaskDetailsDO;
import com.example.workflow.dtos.UserDetails;
import com.example.workflow.dtos.UserProfileDto;

@RestController
@CrossOrigin
public class OrderResource {

	@Autowired
	private ProcessEngine processEngine;

	@Autowired
	private RestTemplate restTemplate;

	@PostMapping("/payment")
	public String doPayment(@RequestBody PaymentDO request) {
		return "Success";
	}

	@PostMapping("/placeOrder")
	public void placeOrder() {
		processEngine.getRuntimeService().createProcessInstanceByKey("PlaceOrder").setVariable("orderId", "1")
				.setVariable("orderAmount", "10").execute();
	}

	@PostMapping("/place-order")
	public ProcessInstanceWithVariablesDto placeOrderDuplicate(@RequestBody OrderDO request) {
		StartProcessInstanceDto startProcessInstanceDto = new StartProcessInstanceDto();
		startProcessInstanceDto.setBusinessKey(UUID.randomUUID().toString());
		startProcessInstanceDto.setWithVariablesInReturn(Boolean.TRUE);
		VariableValueDto variable = new VariableValueDto();
		variable.setType("String");
		variable.setValue(request.getOrderId());
		VariableValueDto variable1 = new VariableValueDto();
		variable1.setType("Integer");
		variable1.setValue(request.getOrderAmount());
		HashMap<String, VariableValueDto> variables = new HashMap<>();
		variables.put("orderId", variable);
		variables.put("orderAmount", variable1);
		startProcessInstanceDto.setVariables(variables);

		ResponseEntity<ProcessInstanceWithVariablesDto> responseEntity = this.restTemplate.exchange(
				"http://localhost:8081/engine-rest/process-definition/key/Approve-Order/start", HttpMethod.POST,
				new HttpEntity<>(startProcessInstanceDto), ProcessInstanceWithVariablesDto.class);
		if (!responseEntity.hasBody()) {
			throw new RuntimeException("failed.initialize.instance");
		} else {
			return responseEntity.getBody();
		}

	}

	@GetMapping("/orders/task/{id}")
	public TaskDetailsDO getTaskDetails(@PathVariable String id) {

		ResponseEntity<TaskDto[]> responseEntity = this.restTemplate.exchange(
				"http://localhost:8081/engine-rest/task?processInstanceId=" + id, HttpMethod.GET, null,
				TaskDto[].class);
		if (!responseEntity.hasBody()) {
			throw new RuntimeException("failed.get.task");
		} else {
//			return responseEntity.getBody();
			List<TaskDto> taskList = Arrays.asList(responseEntity.getBody());
			taskList.get(0).getId();
			return new TaskDetailsDO(taskList.get(0).getAssignee(), taskList.get(0).getId(),
					processEngine.getFormService().getTaskFormData(taskList.get(0).getId()).getFormFields());
		}

	}

	@GetMapping("/orders/task/{id}/claim/{userId}")
	public String claimTask(@PathVariable String id, @PathVariable String userId) {
		UserIdDto body = new UserIdDto();
		body.setUserId(userId);
		HttpEntity<UserIdDto> requestEntity = new HttpEntity<UserIdDto>(body);

		ResponseEntity<Void> responseEntity = this.restTemplate.exchange(
				"http://localhost:8081/engine-rest/task/" + id + "/claim", HttpMethod.POST, requestEntity, Void.class);
		if (responseEntity.getStatusCode() != HttpStatus.NO_CONTENT) {
			throw new RuntimeException("failed.claim.task");
		} else {
			return "Success";
		}
	}

	@PostMapping("/orders/task/{id}/complete")
	public String completeTask(@PathVariable String id, @RequestBody Map<String, Object> request) {
		Map<String, VariableValueDto> variableMap = new HashMap<>();
		request.forEach((key, value) -> {
			VariableValueDto variable = new VariableValueDto();
			variable.setValue(value);
			variableMap.put(key, variable);
		});
		CompleteTaskDto input = new CompleteTaskDto();
		input.setVariables(variableMap);
		HttpEntity<CompleteTaskDto> requestEntity = new HttpEntity<>(input);

		ResponseEntity<Void> responseEntity = this.restTemplate.exchange(
				"http://localhost:8081/engine-rest/task/" + id + "/complete", HttpMethod.POST, requestEntity,
				Void.class);
		if (responseEntity.getStatusCode() != HttpStatus.NO_CONTENT) {
			throw new RuntimeException("failed.complete.task");
		} else {
			return "Success";
		}
	}

	@GetMapping("/groups")
	public List<Task> getGroupInfo() {
		return processEngine.getTaskService().createTaskQuery().taskCandidateGroup("ScrumMasters").list();
//		processEngine.getTaskService().createTaskQuery().taskcan("aditya").list();
	}

	@GetMapping("/tasks/{userId}")
	public List<TaskDetailsDO> getTaskList(@PathVariable String userId) {
		List<TaskDetailsDO> taskList = new ArrayList<>();
		ResponseEntity<UserDetails> responseUser = this.restTemplate.exchange(
				"http://localhost:8081/engine-rest/identity/groups?userId=" + userId, HttpMethod.GET, null,
				UserDetails.class);
		// get un-assigned tasks
		ResponseEntity<TaskDto[]> responseEntity = this.restTemplate
				.exchange(
						"http://localhost:8081/engine-rest/task?candidateGroup="
								+ responseUser.getBody().getGroups().get(0).getId(),
						HttpMethod.GET, null, TaskDto[].class);

		// set un-assigned tasks data
		List<TaskDto> unAssignedTasks = Arrays.asList(responseEntity.getBody());
		unAssignedTasks.stream().forEach(task -> {
			taskList.add(new TaskDetailsDO(task.getAssignee(), task.getId(),
					processEngine.getFormService().getTaskFormData(task.getId()).getFormFields()));
		});

		// get assigned tasks
		ResponseEntity<TaskDto[]> responseEntityAssignedTasks = this.restTemplate.exchange(
				"http://localhost:8081/engine-rest/task?assignee=" + userId, HttpMethod.GET, null, TaskDto[].class);
		
		// set assigned tasks data
		List<TaskDto> assignedTasks = Arrays.asList(responseEntityAssignedTasks.getBody());
		assignedTasks.stream().forEach(task -> {
			taskList.add(new TaskDetailsDO(task.getAssignee(), task.getId(),
					processEngine.getFormService().getTaskFormData(task.getId()).getFormFields()));
		});

		return taskList;

	}

	@GetMapping("/user/{userId}")
	public UserDetails getUserDetails(@PathVariable String userId) {
		ResponseEntity<UserDetails> responseEntity = this.restTemplate.exchange(
				"http://localhost:8081/engine-rest/identity/groups?userId=" + userId, HttpMethod.GET, null,
				UserDetails.class);
		ResponseEntity<UserProfileDto> responseEntity1 = this.restTemplate.exchange(
				"http://localhost:8081/engine-rest/user/" + userId + "/profile", HttpMethod.GET, null,
				UserProfileDto.class);
		responseEntity.getBody().setUserProfile(responseEntity1.getBody());
		return responseEntity.getBody();
	}

}
