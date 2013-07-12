package com.microemple.twitter;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.microtemple.android.lib.common.L;

public class MTTwitterManager {


	private Twitter				twitter;
	private boolean				isAuth	= false;
	private Dialog					alertDialog;
	private SharedPreferences		storage;
	private RequestToken			requestToken;
	private Runnable				onAuthSuccess;
	private static MTTwitterManager	INSTANCE	= null;

	synchronized public static MTTwitterManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new MTTwitterManager();
		}
		return INSTANCE;
	}


	private MTTwitterManager() {

	}

	public MTTwitterManager init(final Context context) {
		if (storage == null) {
			storage = context.getSharedPreferences(
				"com.microtemple.twitter.MTTwitterManager",
				Context.MODE_MULTI_PROCESS);
		}
		return this;
	}


	public void oAuth(final Context context, final Runnable onAuthSuccess,
		final boolean toAuth) {
		isAuth = false;
		if (twitter == null) {
			ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
			configurationBuilder
				.setOAuthConsumerKey("KNd51kiUlA4ninXUjl3hw");
			configurationBuilder
				.setOAuthConsumerSecret("y2Zan5KGb5vfd7iJYLx8RjFD903r8lV5e4H17ndRj4");
			Configuration configuration = configurationBuilder.build();
			twitter = new TwitterFactory(configuration).getInstance();

			AccessToken accessToken = loadAccessToken();
			if (accessToken != null) {
				twitter.setOAuthAccessToken(accessToken);

				new AsyncTask<Object, Object, Object>() {

					@Override
					protected Object doInBackground(Object... params) {
						try {
							twitter.verifyCredentials();
							isAuth = true;
							if (onAuthSuccess != null) {
								onAuthSuccess.run();
							}
						} catch (TwitterException e) {
							L.e(e);
							isAuth = false;
						}
						return isAuth;
					}

					@Override
					protected void onPostExecute(Object result) {
						isAuth = (Boolean) result;
						if (!isAuth && toAuth) {
							openDialog(context);
						}
					}
				}.execute();


			} else {
				if (!isAuth && toAuth) {
					openDialog(context);
				} else {
					if (onAuthSuccess != null) {
						onAuthSuccess.run();
					}
				}
			}
		}

	}

	private void setWebView(WebView webView, String url) {
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.startsWith("piclet://")) {
					Uri uri = Uri.parse(url);
					final String oauth_verifier = uri
						.getQueryParameter("oauth_verifier");
					L.d("oauth_verifier=" + oauth_verifier);
					new AsyncTask<Object, Object, Object>() {

						@Override
						protected Object doInBackground(Object... params) {
							if (requestToken != null && twitter != null) {
								try {
									twitter.getOAuthAccessToken(
										requestToken, oauth_verifier);
									requestToken = null;
									AccessToken token = twitter
										.getOAuthAccessToken();
									storeAccessToken(token);
									// test:							
									L.d("toke=" + token.getToken());
									String name = token
										.getScreenName();
									L.d("Screen Name" + name);
									twitter.updateStatus("hellow world,sucks!"
										+ System.currentTimeMillis());
									isAuth = true;
								} catch (TwitterException e) {
									L.e(e);
									isAuth = false;
								}
							}

							return isAuth;
						}


						@Override
						protected void onPostExecute(Object result) {
							alertDialog.cancel();
							alertDialog = null;

						}

					}.execute();
					return true;
				}

				return false;
			}


		});

		webView.loadUrl(url);
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);

	}

	private AccessToken loadAccessToken() {
		String getToken = storage.getString("getToken", null);
		String getTokenSecret = storage.getString("getTokenSecret", null);

		if (getToken != null && getTokenSecret != null) {
			return new AccessToken(getToken, getTokenSecret);
		}
		return null;
	}

	private void clearAccessToken() {
		storeAccessToken(null);
	}

	private void storeAccessToken(AccessToken token) {
		Editor editor = storage.edit();
		if (token != null) {
			editor.putString("getToken", token.getToken());
			editor.putString("getTokenSecret", token.getTokenSecret());
		} else {
			editor.putString("getToken", null);
			editor.putString("getTokenSecret", null);
		}
		editor.commit();
	}

	private void openDialog(Context context) {
		alertDialog = new Dialog(context);
		alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//alertDialog.setTitle("My great title");
		alertDialog.setCancelable(true);
		alertDialog.setContentView(R.layout.webview_dialog);


		new AsyncTask<Object, Object, String>() {


			@Override
			protected String doInBackground(Object... params) {

				String authenticationURL = null;
				try {
					requestToken = twitter
						.getOAuthRequestToken("piclet://twitter");
					authenticationURL = requestToken
						.getAuthenticationURL();
					L.d("authenticationURL =" + authenticationURL);
				} catch (TwitterException e) {
					L.e(e);
				}


				return authenticationURL;
			}


			@Override
			protected void onPostExecute(String authenticationURL) {
				if (alertDialog != null && authenticationURL != null) {
					WebView webView = (WebView) alertDialog
						.findViewById(R.id.webView1);
					setWebView(webView, authenticationURL);
					alertDialog.show();
				} else {
					L.e("alertDialog=" + alertDialog
						+ " authenticationURL=" + authenticationURL);
				}

			}

		}.execute();


	}

	public Twitter getTwitter() {
		return twitter;
	}


	public boolean isAuth() {
		return isAuth;
	}

	public void clear() {
		twitter = null;
		isAuth = false;
		alertDialog = null;
		requestToken = null;
		clearAccessToken();
	}


}
