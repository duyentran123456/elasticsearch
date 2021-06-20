package com.searchApp.service;

import java.io.IOException;

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
import org.springframework.stereotype.Component;

import com.searchApp.constant.AppConstant;
import com.searchApp.model.AppResponse;
import com.searchApp.utils.AppResponseConverter;
import com.searchApp.utils.HeadlineField;

@Component
public class SearchService {
	private RestHighLevelClient client = new RestHighLevelClient(
			RestClient.builder(new HttpHost("localhost", 9200, "http")));

	// search: normal search, full text search in headlines^2 and description,
	// authors
	public AppResponse search(String q, int from, int size) {
		try {
			SearchRequest request = new SearchRequest(AppConstant.INDEX);
			SearchSourceBuilder sourceBuilder = MySearchSourceBuilder.source(from, size);

			MultiMatchQueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(q)
												.field(HeadlineField.HEADLINE, 2.0f)
												.field(HeadlineField.SHORTDESCRIPTION)
												.field(HeadlineField.AUTHORS)
												.fuzziness(Fuzziness.AUTO);
			sourceBuilder.query(queryBuilder);

			request.source(sourceBuilder);

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
		try {
			SearchRequest request = new SearchRequest(AppConstant.INDEX);
			SearchSourceBuilder sourceBuilder = MySearchSourceBuilder.source(from, size);
			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
			
			// normal search
			if (q != null && q.length() != 0) {
				MultiMatchQueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(q)
						.field(HeadlineField.HEADLINE, 2.0f)
						.field(HeadlineField.SHORTDESCRIPTION)
						.field(HeadlineField.AUTHORS)
						.fuzziness(Fuzziness.AUTO);
				boolQuery.must(queryBuilder);
			}

			// exact word
			if (exact != null && exact.length() != 0) {
				boolQuery.must(QueryBuilders.matchPhraseQuery(HeadlineField.HEADLINE, exact));
			}
			
			// not word
			if(not != null && not.length() != 0) {
				boolQuery.mustNot(QueryBuilders.multiMatchQuery(not)
						.field(HeadlineField.HEADLINE)
						.field(HeadlineField.SHORTDESCRIPTION)
						.field(HeadlineField.AUTHORS));
			}
			
			// in category
			if(category != null && category.length() != 0) {
				boolQuery.filter(QueryBuilders.termQuery(HeadlineField.CATEGORY, category));
			}
			
			// published date range
			if(gtDate != null && ltDate != null) {
				boolQuery.filter(QueryBuilders.rangeQuery(HeadlineField.DATE).gte(gtDate).lte(ltDate));
			}

			sourceBuilder.query(boolQuery);
			request.source(sourceBuilder);
			SearchResponse response = client.search(request, RequestOptions.DEFAULT);

			System.out.println("Search request: " + request.source().toString());
			AppResponse res = AppResponseConverter.convert(response);

			return res;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// autocomplete
	public String autocomplete(String q) {
		return "";
	}
}
