package mesfavoris.model.modification;

import java.util.List;

import com.google.common.collect.Lists;

import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;

public class BookmarksMovedModification extends BookmarksModification {
	private final List<BookmarkId> bookmarkIds;
	private final BookmarkId newParentId;
	
	public BookmarksMovedModification(BookmarksTree sourceTree, BookmarksTree targetTree, BookmarkId newParentId, List<BookmarkId> bookmarkIds) {
		super(sourceTree, targetTree);
		this.newParentId = newParentId;
		this.bookmarkIds = Lists.newArrayList(bookmarkIds);
	}

	public List<BookmarkId> getBookmarkIds() {
		return bookmarkIds;
	}
	
	public BookmarkId getNewParentId() {
		return newParentId;
	}

	@Override
	public String toString() {
		return "BookmarksMovedModification [bookmarkIds=" + bookmarkIds + ", newParentId=" + newParentId + "]";
	}
	
}
