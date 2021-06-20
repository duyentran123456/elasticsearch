package com.searchApp.utils;

import java.time.LocalDate;
import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;

import com.searchApp.model.AppResponse;
import com.searchApp.model.Headline;

public class AppResponseConverter {
	public static AppResponse convert(SearchResponse searchResponse) {
		AppResponse appResponse = new AppResponse();
		appResponse.setStatus(searchResponse.status().getStatus());
		appResponse.setTimedOut(searchResponse.isTimedOut());
		appResponse.setQueryTime(searchResponse.getTook());
		
		SearchHits hits = searchResponse.getHits();
		appResponse.setNResults(hits.getTotalHits().value);
		
		SearchHit[] searchHits = hits.getHits();
		for(SearchHit hit : searchHits) {
			Headline headline = new Headline();
			
			Map<String, Object> sourceAsMap = hit.getSourceAsMap();
			String date = (String) sourceAsMap.get(HeadlineField.DATE);
			headline.setDate(LocalDate.parse(date));
			headline.setShortDescription((String) sourceAsMap.get(HeadlineField.SHORTDESCRIPTION));
			headline.setLink((String) sourceAsMap.get(HeadlineField.LINK));
			headline.setCategory((String) sourceAsMap.get(HeadlineField.CATEGORY));
			headline.setHeadline((String) sourceAsMap.get(HeadlineField.HEADLINE));
			headline.setAuthors((String) sourceAsMap.get(HeadlineField.AUTHORS));
			
			Map<String, HighlightField> highlightFields = hit.getHighlightFields();
		    HighlightField highlight = highlightFields.get(HeadlineField.HEADLINE);
		    if(highlight != null) {
			    Text[] fragments = highlight.fragments();  
			    String fragmentString = fragments[0].string();
			    headline.setHeadline(fragmentString);
		    }	
		    
		    highlight = highlightFields.get(HeadlineField.SHORTDESCRIPTION);
		    if(highlight != null) {
			    Text[] fragments = highlight.fragments();  
			    String fragmentString = fragments[0].string();
			    headline.setShortDescription(fragmentString);
		    }
			
			appResponse.getResults().add(headline);
		}
		
		return appResponse;
	}
}
