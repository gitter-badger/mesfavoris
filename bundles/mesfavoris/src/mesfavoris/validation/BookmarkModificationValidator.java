package mesfavoris.validation;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import mesfavoris.BookmarksPlugin;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.remote.IRemoteBookmarksStore;
import mesfavoris.remote.RemoteBookmarkFolder;
import mesfavoris.remote.RemoteBookmarksStoreManager;
import mesfavoris.remote.IRemoteBookmarksStore.State;

public class BookmarkModificationValidator implements IBookmarkModificationValidator {
	private final RemoteBookmarksStoreManager remoteBookmarksStoreManager;
	
	public BookmarkModificationValidator(RemoteBookmarksStoreManager remoteBookmarksStoreManager) {
		this.remoteBookmarksStoreManager = remoteBookmarksStoreManager;
	}
	
	@Override
	public IStatus validateModification(BookmarksTree bookmarksTree, BookmarkId bookmarkId) {
		Bookmark bookmark = bookmarksTree.getBookmark(bookmarkId);
		if (bookmark == null) {
			return errorStatus("Cannot find bookmark");
		}
		RemoteBookmarkFolder remoteBookmarkFolder = remoteBookmarksStoreManager
				.getRemoteBookmarkFolderContaining(bookmarksTree, bookmarkId);
		if (remoteBookmarkFolder == null) {
			return Status.OK_STATUS;
		}
		IRemoteBookmarksStore remoteBookmarksStore = remoteBookmarksStoreManager.getRemoteBookmarksStore(remoteBookmarkFolder.getRemoteBookmarkStoreId());
		boolean connected = remoteBookmarksStore.getState() == State.connected;
		if (connected) {
			return Status.OK_STATUS;
		} else {
			return errorStatus("Cannot modify bookmark that is under a shared folder not connected");
		}
	}

	private IStatus errorStatus(String message) {
		return new Status(Status.ERROR, BookmarksPlugin.PLUGIN_ID, message);
	}
 	
}
