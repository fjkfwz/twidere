/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.adapter;

import static org.mariotaku.twidere.util.Utils.getBiggerTwitterProfileImage;
import static org.mariotaku.twidere.util.Utils.parseURL;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.AccountsFragment;
import org.mariotaku.twidere.model.AccountViewHolder;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.util.LazyImageLoader;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;

public class AccountsAdapter extends SimpleCursorAdapter {

	private final LazyImageLoader mProfileImageLoader;
	private final SharedPreferences mPreferences;
	private int mUserColorIdx, mProfileImageIdx, mScreenNameIdx;
	private long mDefaultAccountId;
	private final boolean mDisplayHiResProfileImage;
	private int mAccountIdIdx;
	private final boolean mMultiSelectEnabled;

	private final SparseBooleanArray mCheckedItems = new SparseBooleanArray();

	public AccountsAdapter(final Context context, final boolean multi_select) {
		super(context, R.layout.account_list_item, null, new String[] { Accounts.NAME },
				new int[] { android.R.id.text1 }, 0);
		final TwidereApplication application = TwidereApplication.getInstance(context);
		mMultiSelectEnabled = multi_select;
		mProfileImageLoader = application.getProfileImageLoader();
		mPreferences = context.getSharedPreferences(AccountsFragment.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mDisplayHiResProfileImage = context.getResources().getBoolean(R.bool.hires_profile_image);
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		final int color = cursor.getInt(mUserColorIdx);
		final int position = cursor.getPosition();
		final AccountViewHolder holder = (AccountViewHolder) view.getTag();
		holder.screen_name.setText("@" + cursor.getString(mScreenNameIdx));
		holder.checkbox.setVisibility(mMultiSelectEnabled ? View.VISIBLE : View.GONE);
		holder.checkbox.setChecked(mCheckedItems.get(position));
		holder.setAccountColor(color);
		holder.setIsDefault(mDefaultAccountId != -1 && mDefaultAccountId == cursor.getLong(mAccountIdIdx));
		final String profile_image_url_string = cursor.getString(mProfileImageIdx);
		if (mDisplayHiResProfileImage) {
			mProfileImageLoader.displayImage(parseURL(getBiggerTwitterProfileImage(profile_image_url_string)),
					holder.profile_image);
		} else {
			mProfileImageLoader.displayImage(parseURL(profile_image_url_string), holder.profile_image);
		}
		super.bindView(view, context, cursor);
	}

	public long getAccountIdAt(final int position) {
		return ((Cursor) getItem(position)).getLong(mAccountIdIdx);
	}

	public boolean isChecked(final int position) {
		return mCheckedItems.get(position);
	}

	@Override
	public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {

		final View view = super.newView(context, cursor, parent);
		final AccountViewHolder viewholder = new AccountViewHolder(view);
		view.setTag(viewholder);
		return view;
	}

	@Override
	public void notifyDataSetChanged() {
		mDefaultAccountId = mPreferences.getLong(AccountsFragment.PREFERENCE_KEY_DEFAULT_ACCOUNT_ID, -1);
		super.notifyDataSetChanged();
	}

	public void setItemChecked(final int position, final boolean checked) {
		mCheckedItems.put(position, checked);
		notifyDataSetChanged();
	}

	@Override
	public Cursor swapCursor(final Cursor cursor) {
		mCheckedItems.clear();
		if (cursor != null) {
			mAccountIdIdx = cursor.getColumnIndex(Accounts.ACCOUNT_ID);
			mUserColorIdx = cursor.getColumnIndex(Accounts.USER_COLOR);
			mProfileImageIdx = cursor.getColumnIndex(Accounts.PROFILE_IMAGE_URL);
			mScreenNameIdx = cursor.getColumnIndex(Accounts.SCREEN_NAME);
		}
		return super.swapCursor(cursor);
	}
}
