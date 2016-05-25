package org.chabanois.mesfavoris.internal.bookmarktypes;

import java.util.List;

import org.chabanois.mesfavoris.bookmarktype.IGotoBookmark;
import org.chabanois.mesfavoris.model.Bookmark;
import org.eclipse.ui.IWorkbenchWindow;

public class PluginGotoBookmark implements IGotoBookmark {
	private final BookmarkTypeConfigElementLoader loader = new BookmarkTypeConfigElementLoader();
	private GotoBookmark gotoBookmark;
	
	private synchronized GotoBookmark getGotoBookmark() {
		if (gotoBookmark != null) {
			return gotoBookmark;
		}
		List<IGotoBookmark> gotoBookmarks = loader.load("gotoBookmark");
		this.gotoBookmark = new GotoBookmark(gotoBookmarks);
		return gotoBookmark;
	}
	
	@Override
	public boolean gotoBookmark(IWorkbenchWindow window, Bookmark bookmark) {
		return getGotoBookmark().gotoBookmark(window, bookmark);
	}

}
