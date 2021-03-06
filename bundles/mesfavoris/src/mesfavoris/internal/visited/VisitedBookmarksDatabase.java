package mesfavoris.internal.visited;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.osgi.service.event.EventHandler;

import com.google.common.collect.ImmutableMap;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import mesfavoris.BookmarksPlugin;
import mesfavoris.StatusHelper;
import mesfavoris.model.BookmarkDatabase;
import mesfavoris.model.BookmarkId;
import mesfavoris.model.BookmarksTree;
import mesfavoris.model.IBookmarksListener;
import mesfavoris.model.modification.BookmarkDeletedModification;
import mesfavoris.model.modification.BookmarksModification;
import mesfavoris.topics.BookmarksEvents;

/**
 * The visited bookmarks database.
 * 
 * @author cchabanois
 *
 */
public class VisitedBookmarksDatabase {
	private final AtomicReference<VisitedBookmarks> visitedBookmarksMapReference = new AtomicReference<VisitedBookmarks>(
			new VisitedBookmarks());
	private final IBookmarksListener bookmarksListener;
	private final BookmarkDatabase bookmarkDatabase;
	private final File mostVisitedBookmarksFile;
	private final SaveJob saveJob = new SaveJob();
	private final IEventBroker eventBroker;
	private final EventHandler bookmarkVisitedHandler = event -> bookmarkVisited(
			(BookmarkId) event.getProperty("bookmarkId"));

	public VisitedBookmarksDatabase(IEventBroker eventBroker, BookmarkDatabase bookmarkDatabase,
			File mostVisitedBookmarksFile) {
		this.bookmarkDatabase = bookmarkDatabase;
		this.mostVisitedBookmarksFile = mostVisitedBookmarksFile;
		bookmarksListener = modifications -> bookmarksDeleted(filterBookmarksDeleteModifications(modifications.stream())
				.flatMap(modification -> StreamSupport.stream(modification.getDeletedBookmarks().spliterator(), false))
				.map(bookmark -> bookmark.getId()).collect(Collectors.toList()));
		this.eventBroker = eventBroker;
	}

	private Stream<BookmarkDeletedModification> filterBookmarksDeleteModifications(
			Stream<BookmarksModification> stream) {
		return stream.filter(modification -> modification instanceof BookmarkDeletedModification)
				.map(modification -> (BookmarkDeletedModification) modification);
	}

	public void init() {
		try {
			load();
		} catch (IOException e) {
			StatusHelper.logError("Could not load most visited bookmarks", e);
		}
		bookmarkDatabase.addListener(bookmarksListener);
		eventBroker.subscribe(BookmarksEvents.TOPIC_BOOKMARK_VISITED, bookmarkVisitedHandler);
	}

	public void close() throws InterruptedException {
		bookmarkDatabase.removeListener(bookmarksListener);
		eventBroker.unsubscribe(bookmarkVisitedHandler);		
		do { 
			saveJob.join();
		} while (saveJob.getState() != Job.NONE);
	}

	public VisitedBookmarks getVisitedBookmarks() {
		return visitedBookmarksMapReference.get();
	}

	private void bookmarkVisited(BookmarkId bookmarkId) {
		if (bookmarkDatabase.getBookmarksTree().getBookmark(bookmarkId) == null) {
			return;
		}
		VisitedBookmarks visitedBookmarksMap;
		VisitedBookmarks newVisitedBookmarksMap;
		do {
			visitedBookmarksMap = visitedBookmarksMapReference.get();
			VisitedBookmark visitedBookmark = visitedBookmarksMap.get(bookmarkId);
			if (visitedBookmark == null) {
				visitedBookmark = new VisitedBookmark(bookmarkId, 1);
			} else {
				visitedBookmark = new VisitedBookmark(bookmarkId, visitedBookmark.getVisitCount() + 1);
			}
			newVisitedBookmarksMap = visitedBookmarksMap.add(visitedBookmark);
			if (visitedBookmarksMap == newVisitedBookmarksMap) {
				return;
			}
		} while (!visitedBookmarksMapReference.compareAndSet(visitedBookmarksMap, newVisitedBookmarksMap));
		postVisitedBookmarksChanged(visitedBookmarksMap, newVisitedBookmarksMap);
		saveJob.schedule();
	}

	private void bookmarksDeleted(List<BookmarkId> bookmarkIds) {
		if (bookmarkIds.isEmpty()) {
			return;
		}
		VisitedBookmarks visitedBookmarksMap;
		VisitedBookmarks newVisitedBookmarksMap;
		do {
			visitedBookmarksMap = visitedBookmarksMapReference.get();
			newVisitedBookmarksMap = visitedBookmarksMap;
			for (BookmarkId bookmarkId : bookmarkIds) {
				newVisitedBookmarksMap = newVisitedBookmarksMap.delete(bookmarkId);
			}
			if (visitedBookmarksMap == newVisitedBookmarksMap) {
				return;
			}
		} while (!visitedBookmarksMapReference.compareAndSet(visitedBookmarksMap, newVisitedBookmarksMap));
		postVisitedBookmarksChanged(visitedBookmarksMap, newVisitedBookmarksMap);
		saveJob.schedule();
	}

	private void postVisitedBookmarksChanged(VisitedBookmarks visitedBookmarksMap,
			VisitedBookmarks newVisitedBookmarksMap) {
		eventBroker.post(BookmarksEvents.TOPIC_VISITED_BOOKMARKS_CHANGED,
				ImmutableMap.of("before", visitedBookmarksMap, "after", newVisitedBookmarksMap));
	}

	private void load() throws IOException {
		if (!mostVisitedBookmarksFile.exists()) {
			return;
		}
		BookmarksTree bookmarksTree = bookmarkDatabase.getBookmarksTree();
		VisitedBookmarks visitedBookmarks = new VisitedBookmarks();
		JsonReader jsonReader = new JsonReader(new FileReader(mostVisitedBookmarksFile));
		try {
			jsonReader.beginObject();
			while (jsonReader.hasNext()) {
				BookmarkId bookmarkId = new BookmarkId(jsonReader.nextName());
				int count = Integer.parseInt(jsonReader.nextString());
				if (bookmarksTree.getBookmark(bookmarkId) != null) {
					VisitedBookmark visitedBookmark = new VisitedBookmark(bookmarkId, count);
					visitedBookmarks = visitedBookmarks.add(visitedBookmark);
				}
			}
			jsonReader.endObject();
			visitedBookmarksMapReference.set(visitedBookmarks);
		} finally {
			jsonReader.close();
		}
	}

	private void save(IProgressMonitor monitor) throws IOException {
		VisitedBookmarks visitedBookmarks = visitedBookmarksMapReference.get();
		monitor.beginTask("Saving most visited bookmarks", visitedBookmarks.size());
		JsonWriter jsonWriter = new JsonWriter(new FileWriter(mostVisitedBookmarksFile));
		jsonWriter.setIndent("  ");
		try {
			jsonWriter.beginObject();
			for (VisitedBookmark visitedBookmark : visitedBookmarks.getSet()) {
				jsonWriter.name(visitedBookmark.getBookmarkId().toString());
				jsonWriter.value(visitedBookmark.getVisitCount());
				monitor.worked(1);
			}
			jsonWriter.endObject();

		} finally {
			jsonWriter.close();
		}
	}

	private class SaveJob extends Job {

		public SaveJob() {
			super("Save most visited bookmarks");
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				save(monitor);
				return Status.OK_STATUS;
			} catch (IOException e) {
				return new Status(IStatus.ERROR, BookmarksPlugin.PLUGIN_ID, 0, "Could save most visited bookmarks", e);
			} finally {
				monitor.done();
			}
		}

	}

}
