package mesfavoris.internal.operations;

import java.util.List;

import mesfavoris.BookmarksException;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.validation.IBookmarkModificationValidator;

public class DeleteBookmarksOperation {
	private final BookmarkDatabase bookmarkDatabase;
	private final IBookmarkModificationValidator bookmarkModificationValidator;

	public DeleteBookmarksOperation(BookmarkDatabase bookmarkDatabase,
			IBookmarkModificationValidator bookmarkModificationValidator) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.bookmarkModificationValidator = bookmarkModificationValidator;
	}

	public void deleteBookmarks(final List<BookmarkId> selection) throws BookmarksException {
		bookmarkDatabase.modify(bookmarksTreeModifier -> {
			for (BookmarkId bookmarkId : selection) {
				if (bookmarkModificationValidator
						.validateModification(bookmarksTreeModifier.getCurrentTree(), bookmarkId).isOK()) {
					bookmarksTreeModifier.deleteBookmark(bookmarkId, true);
				}
			}
		});
	}

}
