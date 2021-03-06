package mesfavoris.gdrive.connection.auth;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;

import mesfavoris.gdrive.StatusHelper;

public class AuthorizationCodeEclipseApp extends AuthorizationCodeInstalledApp {
	private final IProgressMonitor monitor;
	
	public AuthorizationCodeEclipseApp(AuthorizationCodeFlow flow,
			VerificationCodeReceiver receiver, IProgressMonitor monitor) {
		super(flow, receiver);
		this.monitor = monitor;
	}

	@Override
	protected void onAuthorization(
			AuthorizationCodeRequestUrl authorizationUrl)
			throws IOException {
		monitor.subTask("Please open the following address in your browser:"
				+ authorizationUrl);
		openInExternalBrowser(URI.create(authorizationUrl.build()));
	}

	private void openInExternalBrowser(URI url) {
		String browserId = "gdrive-authorize";
		try {
			IWorkbenchBrowserSupport browserSupport = PlatformUI
					.getWorkbench().getBrowserSupport();

			IWebBrowser browser = browserSupport
					.createBrowser(
							IWorkbenchBrowserSupport.AS_EXTERNAL,
							browserId,
							"Authorizes the application to access user's protected data on GDrive",
							null);
			browser.openURL(url.toURL());
		} catch (PartInitException e) {
			StatusHelper.logError("Could not open browser", e);
		} catch (MalformedURLException e) {
			StatusHelper.logError("Could not open browser", e);
		}

	}	
	
	public static class Provider implements IAuthorizationCodeInstalledAppProvider {

		@Override
		public AuthorizationCodeInstalledApp get(AuthorizationCodeFlow flow, VerificationCodeReceiver receiver,
				IProgressMonitor monitor) {
			return new AuthorizationCodeEclipseApp(flow, receiver, monitor);
		}
		
	}
	
}
