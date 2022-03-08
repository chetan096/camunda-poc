package com.example.workflow.dtos;

import java.util.List;

public class UserDetails {

	private List<GroupDto> groups;
	private UserProfileDto userProfile;

	public UserDetails() {
		super();
	}

	public UserDetails(List<GroupDto> groups, UserProfileDto userProfile) {
		super();
		this.groups = groups;
		this.userProfile = userProfile;
	}

	public List<GroupDto> getGroups() {
		return groups;
	}

	public void setGroups(List<GroupDto> groups) {
		this.groups = groups;
	}

	public UserProfileDto getUserProfile() {
		return userProfile;
	}

	public void setUserProfile(UserProfileDto userProfile) {
		this.userProfile = userProfile;
	}

}
