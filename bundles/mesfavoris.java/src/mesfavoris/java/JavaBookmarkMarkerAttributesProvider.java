package mesfavoris.java;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.JavaCore;

import mesfavoris.bookmarktype.BookmarkMarkerDescriptor;
import mesfavoris.bookmarktype.IBookmarkMarkerAttributesProvider;
import mesfavoris.java.JavaBookmarkLocationProvider.JavaEditorBookmarkLocation;
import mesfavoris.model.Bookmark;

public class JavaBookmarkMarkerAttributesProvider implements IBookmarkMarkerAttributesProvider {

	private final JavaBookmarkLocationProvider locationProvider;

	public JavaBookmarkMarkerAttributesProvider() {
		this(new JavaBookmarkLocationProvider());
	}
	
	public JavaBookmarkMarkerAttributesProvider(
			JavaBookmarkLocationProvider locationProvider) {
		this.locationProvider = locationProvider;
	}

	@Override
	public BookmarkMarkerDescriptor getMarkerDescriptor(Bookmark bookmark) {
		JavaEditorBookmarkLocation location = locationProvider
				.findLocation(bookmark);
		if (location == null) {
			return null;
		}
		if (location.getLineNumber() == null) {
			return null;
		}
		Map attributes = new HashMap();
		JavaCore.addJavaElementMarkerAttributes(attributes, location.getMember());
		attributes.put(IMarker.LINE_NUMBER, new Integer(location.getLineNumber()+1));
		IResource resource = getMarkerResource(location.getMember());
		return new BookmarkMarkerDescriptor(resource, attributes);
	}
	
	private IResource getMarkerResource(IMember member) {
		ICompilationUnit cu = member.getCompilationUnit();
		if (cu != null && cu.isWorkingCopy()) {
			member = (IMember) member.getPrimaryElement();
		}
		IResource res = member.getResource();
		if (res == null) {
			res = ResourcesPlugin.getWorkspace().getRoot();
		} else if (!res.getProject().exists()) {
			res = ResourcesPlugin.getWorkspace().getRoot();
		}
		return res;
	}

}
