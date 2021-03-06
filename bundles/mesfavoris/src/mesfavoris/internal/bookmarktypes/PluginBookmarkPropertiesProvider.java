package mesfavoris.internal.bookmarktypes;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;

public class PluginBookmarkPropertiesProvider implements IBookmarkPropertiesProvider {
	private final BookmarkTypeConfigElementLoader loader = new BookmarkTypeConfigElementLoader();
	private BookmarkPropertiesProvider bookmarkPropertiesProvider;

	private synchronized BookmarkPropertiesProvider getBookmarkPropertiesProvider() {
		if (bookmarkPropertiesProvider != null) {
			return bookmarkPropertiesProvider;
		}
		List<IBookmarkPropertiesProvider> providers = loader.load("propertiesProvider");
		this.bookmarkPropertiesProvider = new BookmarkPropertiesProvider(providers);
		return bookmarkPropertiesProvider;
	}

	@Override
	public void addBookmarkProperties(Map<String, String> bookmarkProperties, IWorkbenchPart part,
			ISelection selection) {
		getBookmarkPropertiesProvider().addBookmarkProperties(bookmarkProperties, part, selection);
	}

}
