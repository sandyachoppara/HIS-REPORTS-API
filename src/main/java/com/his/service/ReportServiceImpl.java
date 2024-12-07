package com.his.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.his.client.ApplicationApiClient;
import com.his.client.CitizenApiClient;
import com.his.client.EdApiClient;
import com.his.dto.ApplicationRegDTO;
import com.his.dto.CitizenDTO;
import com.his.dto.CitizenReports;
import com.his.dto.EligDetermineDTO;
import com.his.dto.SearchCriteriaDto;
import com.itextpdf.text.Document;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.PdfPTable;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class ReportServiceImpl implements ReportService {

	@Autowired
	EdApiClient edClient;

	@Autowired
	CitizenApiClient citizenClient;

	@Autowired
	ApplicationApiClient arClient;

	@Override
	public List<String> getPlanNames() {
		return edClient.getPlanNames();
	}

	@Override
	public List<String> getPlanStatuses() {
		return edClient.getStatuses();
	}

	@Override
	public List<CitizenReports> getCitizens(SearchCriteriaDto criteria) {
		EligDetermineDTO edDto = new EligDetermineDTO();

		if (!criteria.getPlanName().equals("")) {
			edDto.setPlanName(criteria.getPlanName());
		}

		if (!criteria.getPlanStatus().equals("")) {
			edDto.setEligStatus(criteria.getPlanStatus());
		}

//		if(!criteria.getGender().equals("")) {
//			edDto.(criteria.getGender());
//			}

//		if(!criteria.getStartDate().equals(null)) {
//			edDto.setEligStartdate(criteria.getStartDate());
//			}
//		
//		if(!criteria.getEndDate().equals(null)) {
//			edDto.setEligStartdate(criteria.getEndDate());
//			}
//		
		List<EligDetermineDTO> searchEdDetails = edClient.searchEdDetails(edDto);
		List<CitizenReports> reportCitizenList = new ArrayList<CitizenReports>();
		searchEdDetails.forEach(edEntity -> {
			ApplicationRegDTO application = arClient.getApplication(edEntity.getAppNumber());
			CitizenDTO citizen = citizenClient.getCitizen(application.getCitizenId());
			CitizenReports citizenRepo = new CitizenReports();
			BeanUtils.copyProperties(citizen, citizenRepo);
			reportCitizenList.add(citizenRepo);
		});

		return reportCitizenList;
	}

	@Override
	public void generateExcel(HttpServletResponse response) throws IOException {

		List<EligDetermineDTO> allEdDetalil = edClient.getAllEdDetalil();
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Sheet-1");
		HSSFRow headerRow = sheet.createRow(0);

		headerRow.createCell(0).setCellValue("Name");
		headerRow.createCell(1).setCellValue("Plan Name");
		headerRow.createCell(2).setCellValue("Plan Status");
		headerRow.createCell(3).setCellValue("Elig Start Date");
		headerRow.createCell(4).setCellValue("Elig End Date");
		headerRow.createCell(5).setCellValue("Benefit Amount");
		headerRow.createCell(6).setCellValue("Deniel Reason");

		int rowIndex = 1;

		for (EligDetermineDTO ed : allEdDetalil) {
			HSSFRow row = sheet.createRow(rowIndex);
			ApplicationRegDTO application = arClient.getApplication(ed.getAppNumber());
			CitizenDTO citizen = citizenClient.getCitizen(application.getCitizenId());
			row.createCell(0).setCellValue(citizen.getName());
			row.createCell(1).setCellValue(ed.getPlanName());
			row.createCell(2).setCellValue(ed.getEligStatus());
			row.createCell(3).setCellValue(ed.getEligStartdate() != null ? ed.getEligStartdate().toString() : " ");
			row.createCell(4).setCellValue(ed.getEligEndDate() != null ? ed.getEligEndDate().toString() : " ");
			row.createCell(5).setCellValue(ed.getBenefitAmount() != null ? ed.getBenefitAmount() : 0);
			row.createCell(6).setCellValue(ed.getDenialReason());
			rowIndex++;
		}

		ServletOutputStream ops = response.getOutputStream();
		workbook.write(ops);
		ops.close();
		workbook.close();

	}

	@Override
	public void generatePdf(HttpServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		Document pdfDoc = new Document(PageSize.A4);
		ServletOutputStream ops = response.getOutputStream();
		PdfWriter.getInstance(pdfDoc, ops);
		List<EligDetermineDTO> allEdDetalil = edClient.getAllEdDetalil();
		pdfDoc.open();
		Paragraph p = new Paragraph("Citizens Report");
		pdfDoc.add(p);

		PdfPTable table = new PdfPTable(7);

		table.addCell("Citizen Name");
		table.addCell("Plan Name");
		table.addCell("Plan Status");
		table.addCell("Elig Start Date");
		table.addCell("Elig End Date");
		table.addCell("Benefit Amount");
		table.addCell("Deniel Reason");

		for (EligDetermineDTO ed : allEdDetalil) {

			ApplicationRegDTO application = arClient.getApplication(ed.getAppNumber());
			CitizenDTO citizen = citizenClient.getCitizen(application.getCitizenId());
			
			table.addCell(citizen.getName());
			table.addCell(ed.getPlanName());
			table.addCell(ed.getEligStatus());
			table.addCell(ed.getEligStartdate() != null ? ed.getEligStartdate().toString() : " ");
			table.addCell(ed.getEligEndDate() != null ? ed.getEligEndDate().toString() : " ");
			table.addCell(ed.getBenefitAmount() != null ? ed.getBenefitAmount().toString() : "");
			table.addCell(ed.getDenialReason());

		}
		pdfDoc.add(table);
		pdfDoc.close();

	}

}
