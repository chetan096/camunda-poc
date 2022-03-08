package com.example.workflow.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class File {
	
	@Id
	private Long id;
	
	@Column
	private String filename;

}
