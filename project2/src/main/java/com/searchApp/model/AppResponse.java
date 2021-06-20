package com.searchApp.model;

import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.common.unit.TimeValue;

public class AppResponse {
	@Override
	public String toString() {
		return "AppResponse [status=" + status + ", nResults=" + nResults + ", queryTime=" + queryTime + ", timedOut="
				+ timedOut + ", results=" + results + "]";
	}
	private int status;
	private Long nResults;
	private TimeValue queryTime;
	private boolean timedOut;
	private List<Headline> results;
	
	public AppResponse() {
		results = new ArrayList<Headline>();
	}
	
	public Long getNResults() {
		return nResults;
	}
	public void setNResults(Long nResults) {
		this.nResults = nResults;
	}
	public TimeValue getQueryTime() {
		return queryTime;
	}
	public void setQueryTime(TimeValue queryTime) {
		this.queryTime = queryTime;
	}
	public List<Headline> getResults() {
		return results;
	}
	public void setResults(List<Headline> results) {
		this.results = results;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public boolean isTimedOut() {
		return timedOut;
	}
	public void setTimedOut(boolean timedOut) {
		this.timedOut = timedOut;
	}
}
