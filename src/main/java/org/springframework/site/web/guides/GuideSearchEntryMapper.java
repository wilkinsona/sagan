package org.springframework.site.web.guides;

import org.jsoup.Jsoup;
import org.springframework.site.domain.guides.GettingStartedGuide;
import org.springframework.site.search.SearchEntry;
import org.springframework.site.search.SearchEntryMapper;

import java.util.Date;

public class GuideSearchEntryMapper implements SearchEntryMapper<GettingStartedGuide> {

	@Override
	public SearchEntry map(GettingStartedGuide guide) {
		SearchEntry entry = new SearchEntry();
		entry.setTitle(guide.getGuideId());

		String text = Jsoup.parse(guide.getContent()).text();

		entry.setSummary(text.substring(0, Math.min(500, text.length())));
		entry.setRawContent(text);
		entry.setPath(GettingStartedController.getPath(guide));
		// TODO: Can we get a publish date form github?
		entry.setPublishAt(new Date(0L));
		return entry;
	}
}