package com.searchApp.service;

import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;

import com.searchApp.utils.HeadlineField;

public class MySearchSourceBuilder {
	public static SearchSourceBuilder source(int from, int size) {
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.from(from);
		sourceBuilder.size(size);
		
		sourceBuilder.trackTotalHits(true);
		
		HighlightBuilder highlightBuilder = new HighlightBuilder();
		HighlightBuilder.Field highlightTitle = new HighlightBuilder.Field(HeadlineField.SHORTDESCRIPTION);
		highlightBuilder.field(highlightTitle);
		HighlightBuilder.Field highlightHeadline = new HighlightBuilder.Field(HeadlineField.HEADLINE);
		highlightBuilder.field(highlightHeadline);
		HighlightBuilder.Field highlightAuthor = new HighlightBuilder.Field(HeadlineField.AUTHORS);
		highlightBuilder.field(highlightAuthor);

		highlightBuilder.preTags("<b>");
		highlightBuilder.postTags("</b>");
		sourceBuilder.highlighter(highlightBuilder);
		
		return sourceBuilder;
	}
}
