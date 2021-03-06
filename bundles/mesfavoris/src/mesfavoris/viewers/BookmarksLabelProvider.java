package mesfavoris.viewers;

import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Predicate;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import mesfavoris.bookmarktype.IBookmarkLabelProvider;
import mesfavoris.commons.core.AdapterUtils;
import mesfavoris.internal.views.StylerProvider;
import mesfavoris.model.Bookmark;
import mesfavoris.model.BookmarkFolder;

public class BookmarksLabelProvider extends StyledCellLabelProvider {
	private static final String ELLIPSES = " ... ";
	private final IBookmarkLabelProvider bookmarkLabelProvider;
	private final ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources());
	private final Color commentColor;
	private final StylerProvider stylerProvider = new StylerProvider();
	private final Font boldFont;
	private final Color disabledColor;
	private final Predicate<Bookmark> selectedBookmarkPredicate;
	private final Predicate<Bookmark> disabledBookmarkPredicate;
	private final IBookmarkDecorationProvider bookmarkDecorationProvider;
	private final IBookmarkCommentProvider bookmarkCommentProvider;

	public BookmarksLabelProvider(Predicate<Bookmark> selectedBookmarkPredicate,
			Predicate<Bookmark> disabledBookmarkPredicate, IBookmarkDecorationProvider bookmarkDecorationProvider,
			IBookmarkLabelProvider bookmarkLabelProvider, IBookmarkCommentProvider bookmarkCommentProvider) {
		this.selectedBookmarkPredicate = selectedBookmarkPredicate;
		this.bookmarkLabelProvider = bookmarkLabelProvider;
		this.disabledBookmarkPredicate = disabledBookmarkPredicate;
		this.bookmarkDecorationProvider = bookmarkDecorationProvider;
		this.bookmarkCommentProvider = bookmarkCommentProvider;
		this.boldFont = JFaceResources.getFontRegistry().getBold(JFaceResources.DEFAULT_FONT);
		this.disabledColor = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_GRAY);

		this.commentColor = new Color(PlatformUI.getWorkbench().getDisplay(), 63, 127, 95);
	}

	@Override
	public void update(ViewerCell cell) {
		Bookmark element = (Bookmark) AdapterUtils.getAdapter(cell.getElement(), Bookmark.class);
		StyledString styledText = getStyledText(element);
		cell.setText(styledText.toString());
		cell.setStyleRanges(styledText.getStyleRanges());
		cell.setImage(getImage(element));
		super.update(cell);
	}

	private StyledString getStyledText(final Bookmark bookmark) {
		String comment = bookmarkCommentProvider.apply(bookmark);
		boolean hasComment = comment != null && comment.trim().length() > 0;
		boolean isDisabled = disabledBookmarkPredicate.test(bookmark);
		boolean isSelectedBookmark = selectedBookmarkPredicate.test(bookmark);
		StyledString styledString = bookmarkLabelProvider.getStyledText(bookmark);
		if (isDisabled || isSelectedBookmark) {
			Color color = null;
			Font font = null;
			if (isDisabled) {
				color = disabledColor;
			}
			if (isSelectedBookmark) {
				font = boldFont;
			}
			styledString.setStyle(0, styledString.length(), stylerProvider.getStyler(font, color, null));
		}

		if (hasComment) {
			Color color = commentColor;
			Font font = null;
			if (isDisabled) {
				color = disabledColor;
			}
			if (isSelectedBookmark) {
				font = boldFont;
			}
			styledString.append(" - " + comment, stylerProvider.getStyler(font, color, null));
		}
		return styledString;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		bookmarkLabelProvider.dispose();
		resourceManager.dispose();
		commentColor.dispose();
	}

	private Image getImage(final Bookmark bookmark) {
		Image image = null;
		if (bookmark instanceof BookmarkFolder) {
			String imageKey = ISharedImages.IMG_OBJ_FOLDER;
			image = PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
		} else {
			image = bookmarkLabelProvider.getImage(bookmark);
			if (image == null) {
				String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
				image = PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
			}
		}
		ImageDescriptor[] overlayImages = bookmarkDecorationProvider.apply(bookmark);
		DecorationOverlayIcon decorated = new DecorationOverlayIcon(image, overlayImages);
		return (Image) this.resourceManager.get(decorated);
	}

	public static class DefaultBookmarkCommentProvider implements IBookmarkCommentProvider {

		@Override
		public String apply(Bookmark bookmark) {
			String comment = bookmark.getPropertyValue(Bookmark.PROPERTY_COMMENT);
			if (comment == null) {
				return null;
			}
			try (Scanner scanner = new Scanner(comment)) {
				return scanner.nextLine();
			} catch (NoSuchElementException e) {
				return null;
			}
		}

	}

}