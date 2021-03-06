/**
 * (c) Copyright HCL Technologies Ltd. 2020.
 * LICENSE: Apache License, Version 2.0 https://www.apache.org/licenses/LICENSE-2.0
 */

package com.hcl.appscan.bamboo.plugin.auth;

import com.atlassian.bamboo.credentials.CredentialsData;
import com.hcl.appscan.bamboo.plugin.util.Utility;
import com.hcl.appscan.sdk.auth.AuthenticationHandler;
import com.hcl.appscan.sdk.auth.IAuthenticationProvider;
import com.hcl.appscan.sdk.auth.LoginType;
import com.hcl.appscan.sdk.utils.SystemUtil;

import java.io.Serializable;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

public class BambooAuthenticationProvider implements IAuthenticationProvider, Serializable {
	AppScanAPICredentials credential;
	String token;

	public BambooAuthenticationProvider(AppScanAPICredentials credential) {
		this.credential = credential;
		token = null;
	}

	@Override
	public boolean isTokenExpired() {
		boolean isExpired = false;
		AuthenticationHandler handler = new AuthenticationHandler(this);

		try {
			isExpired = handler.isTokenExpired() && !handler.login(credential.getKeyId(), credential.getPassword(), true, LoginType.ASoC_Federated);
		} catch (Exception e) {
			isExpired = false;
		}
		return isExpired;
	}

	@Override
	public Map<String, String> getAuthorizationHeader(boolean persist) {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Authorization", "Bearer " + getToken().trim()); //$NON-NLS-1$ //$NON-NLS-2$
		if (persist)
			headers.put("Connection", "Keep-Alive"); //$NON-NLS-1$ //$NON-NLS-2$
		return headers;
	}

	private String getToken() {
		return token == null ? "" : token;
	}

	@Override
	public String getServer() {
		return SystemUtil.getServer(credential.getKeyId());
	}

	@Override
	public void saveConnection(String s) {
		token = s;
	}

	@Override
	public Proxy getProxy() {
		return Proxy.NO_PROXY;
	}
}
