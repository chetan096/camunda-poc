package com.example.workflow.dtos;

import java.util.List;

import org.camunda.bpm.engine.form.FormField;

public class TaskDetailsDO {
	
	private String assignee;
	private String taskId;
	private List<FormField> formFields;
	
	
	
	public TaskDetailsDO(String assignee, String taskId, List<FormField> formFields) {
		super();
		this.assignee = assignee;
		this.taskId = taskId;
		this.formFields = formFields;
	}
	public String getAssignee() {
		return assignee;
	}
	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}
	public String getTaskId() {
		return taskId;
	}
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}
	public List<FormField> getFormFields() {
		return formFields;
	}
	public void setFormFields(List<FormField> formFields) {
		this.formFields = formFields;
	}
	
	

}
