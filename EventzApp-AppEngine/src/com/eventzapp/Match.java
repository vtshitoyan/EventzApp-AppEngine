package com.eventzapp;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Match {
	@Id
	private Long id;
	private Long eid;
	private Long matchTypeId;
	private Float match;
	public Match() {
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getEid() {
		return eid;
	}
	public void setEid(Long eid) {
		this.eid = eid;
	}
	public Long getMatchTypeId() {
		return matchTypeId;
	}
	public void setMatchTypeId(Long matchTypeId) {
		this.matchTypeId = matchTypeId;
	}
	public Float getMatch() {
		return match;
	}
	public void setMatch(Float match) {
		this.match = match;
	}
	
	
}
