package mesfavoris.internal.bookmarktypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import mesfavoris.bookmarktype.IBookmarkPropertiesProvider;

public class BookmarkPropertiesProvider implements IBookmarkPropertiesProvider {
	private final List<IBookmarkPropertiesProvider> providers;

	public BookmarkPropertiesProvider(List<IBookmarkPropertiesProvider> providers) {
		this.providers = new ArrayList<IBookmarkPropertiesProvider>(providers);
	}

	@Override
	public void addBookmarkProperties(Map<String, String> bookmarkProperties, IWorkbenchPart part,
			ISelection selection) {
		providers.forEach(p -> p.addBookmarkProperties(bookmarkProperties, part, selection));
	}

}
