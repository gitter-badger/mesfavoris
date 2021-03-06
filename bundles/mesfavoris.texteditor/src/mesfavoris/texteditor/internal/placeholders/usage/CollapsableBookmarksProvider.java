package mesfavoris.texteditor.internal.placeholders.usage;

import static mesfavoris.texteditor.TextEditorBookmarkProperties.PROP_FILE_PATH;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.core.runtime.IPath;

import mesfavoris.model.Bookmark;
import mesfavoris.texteditor.placeholders.PathPlaceholderResolver;

/**
 * Get the bookmarks for which path is collapsable using given placeholder
 * 
 * @author cchabanois
 *
 */
public class CollapsableBookmarksProvider {
	private final String pathPlaceholderName;
	private final PathPlaceholderResolver pathPlaceholderResolver;

	public CollapsableBookmarksProvider(PathPlaceholderResolver pathPlaceholderResolver, String pathPlaceholderName) {
		this.pathPlaceholderName = pathPlaceholderName;
		this.pathPlaceholderResolver = pathPlaceholderResolver;
	}

	public List<Bookmark> getCollapsableBookmarks(Iterable<Bookmark> bookmarks) {
		return StreamSupport.stream(bookmarks.spliterator(), false).filter(this::isCollapsable)
				.collect(Collectors.toList());
	}

	private boolean isCollapsable(Bookmark bookmark) {
		String filePath = bookmark.getPropertyValue(PROP_FILE_PATH);
		if (filePath == null) {
			return false;
		}
		String variableName = PathPlaceholderResolver.getPlaceholderName(filePath);
		if (pathPlaceholderName.equals(variableName)) {
			return false;
		}
		IPath path = pathPlaceholderResolver.expand(filePath);
		if (path == null) {
			return false;
		}
		String collapsedPath = pathPlaceholderResolver.collapse(path, pathPlaceholderName);
		variableName = PathPlaceholderResolver.getPlaceholderName(collapsedPath);
		return pathPlaceholderName.equals(variableName);
	}

}
