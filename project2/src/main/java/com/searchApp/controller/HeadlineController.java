package com.searchApp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.searchApp.constant.AppConstant;
import com.searchApp.model.AppResponse;
import com.searchApp.service.SearchService;

@Controller
public class HeadlineController {

	@Autowired
	private SearchService service;

	@GetMapping(path = "/")
	public String home(Model model) {
		return "index";
	}

	
	
	@GetMapping(path = "/search")
	public String search(@RequestParam(name = "q", required = false) String query,
			@RequestParam(name = "page", required = false) String page, Model model) {
		if (query== null || query.length() == 0)
			return "index";
		int from = 0, size = AppConstant.PAGE_SIZE;
		if(page == null || page.length() != 0) {
			try {
				int nPage = Integer.parseInt(page);
				from = (nPage-1) * size;
			} catch(NumberFormatException e) {
				
			}
		}

		AppResponse appResponse = service.search(query, from, size);

		if (appResponse == null) {
			System.out.println("app response is null");
			return "index";
		}
		System.out.println(appResponse.toString());
		long nPages = appResponse.getNResults()/size;
		model.addAttribute("appResponse", appResponse);
		model.addAttribute("nPages", nPages);

		return "results";
	}

	
	
	@GetMapping(path = "/advanced_search")
	public String advancedSearch(@RequestParam(name = "q", required = false) String query,
			@RequestParam(name = "exact", required = false) String exact,
			@RequestParam(name = "not", required = false) String not,
			@RequestParam(name = "category", required = false) String category,
			@RequestParam(name = "gtDate", required = false) String gtDate,
			@RequestParam(name = "ltDate", required = false) String ltDate,
			@RequestParam(name = "page", required = false) String page, Model model) {
		if (query == null || query.length() == 0)
			return "advanced";
		
		int from = 0, size = AppConstant.PAGE_SIZE;
		if(page == null || page.length() != 0) {
			try {
				int nPage = Integer.parseInt(page);
				from = (nPage-1) * size;
			} catch(NumberFormatException e) {
				
			}
		}
		
		AppResponse appResponse = service.advancedSearch(query, exact, not, category, gtDate, ltDate, from, size);

		if (appResponse == null) {
			System.out.println("app response is null");
			return "index";
		}
		System.out.println(appResponse.toString());
		long nPages = appResponse.getNResults()/size;
		model.addAttribute("appResponse", appResponse);
		model.addAttribute("nPages", nPages);

		return "results";
	}

	
	
	// @GetMapping(path="/autocomplete")

}
