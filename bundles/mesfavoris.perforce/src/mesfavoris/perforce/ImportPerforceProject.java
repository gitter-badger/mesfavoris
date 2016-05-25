package mesfavoris.perforce;

import static mesfavoris.perforce.PerforceProjectProperties.PROP_PATH;
import static mesfavoris.perforce.PerforceProjectProperties.PROP_PORT;

import java.text.MessageFormat;

import org.chabanois.mesfavoris.BookmarksException;
import org.chabanois.mesfavoris.bookmarktype.AbstractImportTeamProject;
import org.chabanois.mesfavoris.model.Bookmark;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;

import com.perforce.team.ui.PerforceProjectSetSerializer;

public class ImportPerforceProject extends AbstractImportTeamProject {

	private String[] getReferenceStrings(Bookmark bookmark) {
		return new String[] {
				MessageFormat.format("PORT={0};NAME={1};PATH={2}", bookmark.getPropertyValue(PROP_PORT),
						bookmark.getPropertyValue("projectName"), bookmark.getPropertyValue(PROP_PATH))
		};
	}	
	
	@Override
	public void importProject(Bookmark bookmark, IProgressMonitor monitor)
			throws BookmarksException {
		PerforceProjectSetSerializer perforceProjectSetSerializer = new PerforceProjectSetSerializer();
		try {
			perforceProjectSetSerializer.addToWorkspace(getReferenceStrings(bookmark), null, monitor);
		} catch (TeamException e) {
			throw new BookmarksException("Could not import perforce project", e);
		}
	}

	@Override
	public boolean canHandle(Bookmark bookmark) {
		return bookmark.getPropertyValue(PROP_PATH) != null
				&& bookmark.getPropertyValue(PROP_PORT) != null;
	}

}
