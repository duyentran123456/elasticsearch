package com.searchApp.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion.Entry;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.springframework.stereotype.Component;

import com.searchApp.constant.AppConstant;
import com.searchApp.constant.HeadlineField;
import com.searchApp.model.AppResponse;
import com.searchApp.utils.AppResponseConverter;

@Component
public class SearchService {
	private RestHighLevelClient client = new RestHighLevelClient(
			RestClient.builder(new HttpHost("localhost", 9200, "http")));

	// search: normal search, full text search in headlines^2 and description,
	// authors
	public AppResponse search(String q, int from, int size) {

		SearchRequest request = new SearchRequest(AppConstant.INDEX);
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

		MultiMatchQueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(q).field(HeadlineField.HEADLINE, 2.0f)
				.field(HeadlineField.SHORTDESCRIPTION).field(HeadlineField.AUTHORS).fuzziness(Fuzziness.AUTO);
		sourceBuilder.query(queryBuilder);

		request.source(sourceBuilder);
		
		
		try {
			SearchResponse search = client.search(request, RequestOptions.DEFAULT);
			AppResponse res = AppResponseConverter.convert(search);
			return res;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// advanced search: in a range of time; category name; by author; exact term;
	// not have this term; ..
	public AppResponse advancedSearch(String q, String exact, String not, String category, String gtDate, String ltDate,
			int from, int size) {

		SearchRequest request = new SearchRequest(AppConstant.INDEX);
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
		
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

		// normal search
		if (q != null && q.length() != 0) {
			MultiMatchQueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(q).field(HeadlineField.HEADLINE, 2.0f)
					.field(HeadlineField.SHORTDESCRIPTION).field(HeadlineField.AUTHORS).fuzziness(Fuzziness.AUTO);
			boolQuery.must(queryBuilder);
		}

		// exact word
		if (exact != null && exact.length() != 0) {
			boolQuery.must(QueryBuilders.matchPhraseQuery(HeadlineField.HEADLINE, exact));
		}

		// not word
		if (not != null && not.length() != 0) {
			boolQuery.mustNot(QueryBuilders.multiMatchQuery(not).field(HeadlineField.HEADLINE)
					.field(HeadlineField.SHORTDESCRIPTION).field(HeadlineField.AUTHORS));
		}

		// in category
		if (category != null && category.length() != 0) {
			boolQuery.filter(QueryBuilders.termQuery(HeadlineField.CATEGORY, category));
		}

		// published date range
		if (gtDate != null && ltDate != null && gtDate.length() != 0 && ltDate.length() != 0) {
			boolQuery.filter(QueryBuilders.rangeQuery(HeadlineField.DATE).gte(gtDate).lte(ltDate));
		}

		sourceBuilder.query(boolQuery);
		request.source(sourceBuilder);
		
		try {
			SearchResponse response = client.search(request, RequestOptions.DEFAULT);
			AppResponse res = AppResponseConverter.convert(response);

			return res;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// autocomplete
	public List<String> autocomplete(String q) {
		List<String> res = new ArrayList<String>();
		
		SearchRequest request = new SearchRequest(AppConstant.INDEX);
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		
		SuggestBuilder suggestBuilder = new SuggestBuilder();
		CompletionSuggestionBuilder completeSuggestionBuilder = SuggestBuilders.completionSuggestion(HeadlineField.CATEGORY)
																.prefix(q)
																.size(AppConstant.SUGGEST_NUM)
																.skipDuplicates(true);
		suggestBuilder.addSuggestion("category-suggest", completeSuggestionBuilder);
		sourceBuilder.suggest(suggestBuilder);
		request.source(sourceBuilder);
		SearchResponse response = null;
		
		try {
			response = client.search(request, RequestOptions.DEFAULT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Suggest suggest = response.getSuggest();
		if(suggest != null) {
			CompletionSuggestion entries = suggest.getSuggestion("category-suggest");
			for(Entry entry: entries) {
				for (CompletionSuggestion.Entry.Option option : entry.getOptions()) {
					res.add( option.getText().string().toLowerCase() );
				}
			}
		}
		
		return res;
		
	}
}
