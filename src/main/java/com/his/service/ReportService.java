package com.his.service;

import java.io.IOException;
import java.util.List;

import com.his.dto.CitizenReports;
import com.his.dto.SearchCriteriaDto;

import jakarta.servlet.http.HttpServletResponse;

public interface ReportService {
	
	public List<String> getPlanNames();
	public List<String> getPlanStatuses();
	public List<CitizenReports> getCitizens(SearchCriteriaDto criteria);
	public void generateExcel(HttpServletResponse response)throws IOException;
	public void generatePdf(HttpServletResponse response)throws Exception;
	

}
