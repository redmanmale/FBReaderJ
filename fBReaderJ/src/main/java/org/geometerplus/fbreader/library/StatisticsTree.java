/*
 * Copyright (C) 2009-2014 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.fbreader.library;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.geometerplus.android.util.ViewUtil;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.fbreader.book.*;
import org.geometerplus.fbreader.tree.FBTree;

//BookTree

public class StatisticsTree extends LibraryTree {
	public static ZLResource resource() {
		return ZLResource.resource("library").getResource(ROOT_STATISTICS);
	}

	public final String entryTitle;
	public final String entrySummary;
	public final boolean isSelectable;

	StatisticsTree(LibraryTree parent, String title, String summary, boolean selectable) {
		super(parent);
		hasUniqueView = true;
		entryTitle = StatisticsTree.resource().getResource(title).getValue();
		entrySummary = summary;
		isSelectable = selectable;
	}

	StatisticsTree(LibraryTree parent, String title, String summary) {
		this(parent, title, summary, false);
	}

	@Override
	public Status getOpeningStatus() {
		return isSelectable ? Status.READY_TO_OPEN : Status.WAIT_FOR_OPEN;
	}

	@Override
	public String getName() {
		return entryTitle;
	}

	@Override
	public String getSummary() {
		return entrySummary;
	}

	@Override
	public Book getBook() {
		return null;
	}

	@Override
	protected String getStringId() {
		Log.d("checkUsed", "getstringid");
		return ROOT_STATISTICS + entryTitle;
	}

	@Override
	protected ZLImage createCover() {
		Log.d("checkUsed", "createcover");
		return null;
	}

	@Override
	public View createUniqueView(View convertView, ViewGroup parent, LibraryTree tree) {
		final View view = (convertView != null) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.library_tree_item, parent, false);

		final TextView nameView = ViewUtil.findTextView(view, R.id.library_tree_item_name);
		nameView.setText(tree.getName());

		final TextView summaryView = ViewUtil.findTextView(view, R.id.library_tree_item_childrenlist);
		SpannableString test = new SpannableString(tree.getSummary());
		test.setSpan(new StyleSpan(Typeface.BOLD), 0, 6, 0);
		summaryView.setText(test);

		return view;
	}

	// if statistics page contains mentioned book
	@Override
	public boolean containsBook(Book book) {
		Log.d("checkUsed", "containsbook");
		return false;
	}

	// no need?
	@Override
	protected String getSortKey() {
		Log.d("checkUsed", "getsortkey");
		return ROOT_STATISTICS;
	}

	// no need?
	@Override
	public int compareTo(FBTree tree) {
		Log.d("checkUsed", "compareto");
		return -9000;
	}

	// no need?
	@Override
	public boolean equals(Object object) {
		Log.d("checkUsed", "equals");
		return false;
	}
}
