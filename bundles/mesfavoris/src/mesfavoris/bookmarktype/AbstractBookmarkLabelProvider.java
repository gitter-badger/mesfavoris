package mesfavoris.bookmarktype;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;

import mesfavoris.model.Bookmark;

public abstract class AbstractBookmarkLabelProvider extends LabelProvider
		implements IBookmarkLabelProvider {

	@Override
	public StyledString getStyledText(Object element) {
		Bookmark bookmark = (Bookmark) element;
		String name = bookmark.getPropertyValue(Bookmark.PROPERTY_NAME);
		if (name == null) {
			name = "unnamed";
		}
		return new StyledString(name);
	}	
	
}
