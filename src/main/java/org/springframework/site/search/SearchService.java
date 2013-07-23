package org.springframework.site.search;

import io.searchbox.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Delete;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.indices.CreateIndex;
import io.searchbox.indices.DeleteIndex;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

	private static final String INDEX = "site";

	private static Log logger = LogFactory.getLog(SearchService.class);

	private final SearchQueryBuilder searchQueryBuilder = new SearchQueryBuilder();
	private final JestClient jestClient;

	private boolean useRefresh = false;
	private SearchResultParser searchResultParser;

	@Autowired
	public SearchService(JestClient jestClient, SearchResultParser searchResultParser) {
		this.jestClient = jestClient;
		this.searchResultParser = searchResultParser;
	}

	public void saveToIndex(SearchEntry entry) {
		Index.Builder newIndexBuilder = new Index.Builder(entry).id(entry.getId()).index(INDEX)
				.type("site");

		if (this.useRefresh) {
			newIndexBuilder.refresh(true);
		}
		execute(newIndexBuilder.build());
	}

	private JestResult execute(Action action) {
		try {
			JestResult result = this.jestClient.execute(action);
			logger.debug(result.getJsonString());
			return result;
		} catch (Exception e) {
			throw new SearchException(e);
		}
	}

	public Page<SearchResult> search(String term, Pageable pageable) {
		Search.Builder searchBuilder;
		if (term.equals("")) {
			searchBuilder = this.searchQueryBuilder.forEmptyQuery(pageable);
		} else {
			searchBuilder = this.searchQueryBuilder.forQuery(term, pageable);
		}
		searchBuilder.addIndex(INDEX);
		JestResult jestResult = execute(searchBuilder.build());
		List<SearchResult> searchResults = searchResultParser.parseResults(jestResult);

		int totalResults = jestResult.getJsonObject().getAsJsonObject("hits").get("total").getAsInt();
		return new PageImpl<>(searchResults, pageable, totalResults);
	}

	public void deleteIndex() {
		execute(new DeleteIndex.Builder(INDEX).build());
	}

	public void createIndex() {
		execute(new CreateIndex.Builder(INDEX).build());

	}

	public void setUseRefresh(boolean useRefresh) {
		this.useRefresh = useRefresh;
	}

	public void removeFromIndex(SearchEntry entry) {
		Delete delete = new Delete.Builder().id(entry.getId()).index(INDEX).type("site") // TODO
																					// this
																					// should
																					// come
																					// from
																					// the
																					// 'entry'
				.build();

		execute(delete);
	}
}