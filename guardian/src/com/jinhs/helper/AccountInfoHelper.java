package com.jinhs.helper;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

/**
 * This class uses the AccountManager to get the primary email address of the
 * current user.
 */
public class AccountInfoHelper {
	
	private static String userEmail;

	public static String getEmail(Context context) {
		if(userEmail!=null)
			return userEmail;
		
		AccountManager accountManager = AccountManager.get(context);
		Account account = getAccount(accountManager);

		if (account == null) {
			return null;
		} else {
			userEmail = account.name;
			return userEmail;
		}
	}

	private static Account getAccount(AccountManager accountManager) {
		Account[] accounts = accountManager.getAccountsByType("com.google");
		Account account;
		if (accounts.length > 0) {
			account = accounts[0];
		} else {
			account = null;
		}
		return account;
	}
}
