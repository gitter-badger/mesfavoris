package org.chabanois.mesfavoris.internal.operations;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.chabanois.mesfavoris.internal.model.copy.BookmarksCopier;
import org.chabanois.mesfavoris.internal.model.utils.BookmarksTreeUtils;
import org.chabanois.mesfavoris.model.BookmarkFolder;
import org.chabanois.mesfavoris.model.BookmarkId;
import org.chabanois.mesfavoris.model.BookmarksTree;
import org.chabanois.mesfavoris.model.modification.BookmarksTreeModifier;
import org.chabanois.mesfavoris.persistence.json.BookmarksTreeJsonSerializer;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

/**
 * Copy a set of bookmarks to the clipboard
 * 
 * @author cchabanois
 *
 */
public class CopyBookmarkOperation {

	public void copyToClipboard(BookmarksTree bookmarksTree, List<BookmarkId> selection) {
		if (selection.size() == 0) {
			return;
		}
		Clipboard clipboard = new Clipboard(null);
		try {
			TextTransfer textTransfer = TextTransfer.getInstance();
			Transfer[] transfers = new Transfer[] { textTransfer };
			Object[] data = new Object[] { getSelectionAsJson(bookmarksTree, selection) };
			clipboard.setContents(data, transfers);
		} finally {
			clipboard.dispose();
		}
	}

	public boolean hasDuplicatedBookmarksInSelection(BookmarksTree bookmarksTree, List<BookmarkId> selection) {
		return selection.size() != BookmarksTreeUtils.getUnduplicatedBookmarks(bookmarksTree, selection).size();
	}

	private String getSelectionAsJson(BookmarksTree bookmarksTree, List<BookmarkId> selection) {
		BookmarksTree selectionAsBookmarksTree = getSelectionAsBookmarksTree(bookmarksTree, selection);
		BookmarksTreeJsonSerializer serializer = new BookmarksTreeJsonSerializer(true);
		StringWriter writer = new StringWriter();
		try {
			serializer.serialize(selectionAsBookmarksTree, selectionAsBookmarksTree.getRootFolder().getId(), writer,
					new NullProgressMonitor());
		} catch (IOException e) {
			return null;
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
			}
		}
		return writer.toString();
	}

	private BookmarksTree getSelectionAsBookmarksTree(BookmarksTree bookmarksTree, final List<BookmarkId> selection) {
		BookmarkFolder bookmarkFolder = new BookmarkFolder(new BookmarkId("selection"), "selection");
		BookmarksTreeModifier bookmarksTreeModifier = new BookmarksTreeModifier(new BookmarksTree(bookmarkFolder));
		BookmarksCopier bookmarksCopier = new BookmarksCopier(bookmarksTree, id -> id);
		bookmarksCopier.copy(bookmarksTreeModifier, bookmarkFolder.getId(),
				BookmarksTreeUtils.getUnduplicatedBookmarks(bookmarksTree, selection));
		return bookmarksTreeModifier.getCurrentTree();
	}

}
