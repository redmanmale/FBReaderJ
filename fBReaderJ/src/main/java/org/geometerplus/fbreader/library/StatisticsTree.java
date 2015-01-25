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

import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.geometerplus.android.fbreader.covers.CoverManager;
import org.geometerplus.android.fbreader.tree.TreeActivity;
import org.geometerplus.android.util.ViewUtil;
import org.geometerplus.zlibrary.core.image.ZLImage;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.RationalNumber;
import org.geometerplus.zlibrary.ui.android.R;
import org.geometerplus.fbreader.book.*;
import org.geometerplus.fbreader.tree.FBTree;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StatisticsTree extends LibraryTree {
	private static CoverManager sharedCoverManager;

	public static ZLResource resource() {
		return ZLResource.resource("library").getResource(ROOT_STATISTICS);
	}

	// enums must have the same name as resource keys
	public static enum Types { progress, completed, reading, average, library }
	public final Types myType;
	public final String entryTitle;
	public final boolean isSelectable;
	private final static float headingSize = 1.7f;

	// progress
	public static Book mostRecentBook;
	public static int mostRecentBookPercentRead;
	public static SpannableString mostRecentBookSummary;
	public static int mostRecentBookTimeSpent;
	public static int mostRecentBookPagesTurned;
	// completed
	public final static Filter booksCompletedFilter = new Filter.ByCompletion(1.00f);
	public static List<Book> booksCompleted;
	public final static Filter booksReadingFilter = new Filter.ByLabel(Book.READ_LABEL);
	public static List<Book> booksReading;
	public static int numPagesTurned;
	public static int hoursSpentTotal;
	public static int percentLibraryCompleted;
	//
	public final static Filter booksFavoritedFilter = new Filter.ByLabel(Book.FAVORITE_LABEL);
	public static List<Book> booksFavorited;
	StatisticsTree(LibraryTree parent, Types type, boolean selectable) {
		super(parent);

		myType = type;
		isSelectable = selectable;
		entryTitle = StatisticsTree.resource().getResource(type.name()).getValue();

		switch(myType) {
			case progress: {
				mostRecentBook = Collection.getRecentBook(0);
				if(mostRecentBook != null) {
					final BookStatistics stats = mostRecentBook.getStatistics();
					Log.d("check6", "Book: " + mostRecentBook.getTitle());
					Log.d("check6", "Date Added: " + new Date(stats.getDateAdded()).toString());
					Log.d("check6", "Date Opened: " + new Date(stats.getDateOpened()).toString());
					Log.d("check6", "Date Closed: " + new Date(stats.getDateClosed()).toString());
					final RationalNumber progress = mostRecentBook.getProgress();
					mostRecentBookPercentRead = (int)(progress != null ? progress.toFloat() * 100 : 0);
					String recentBookTitle = mostRecentBook.getTitle();
					// TODO: show all authors
					mostRecentBookSummary = new SpannableString(recentBookTitle + "\n" +
							mostRecentBook.authors().get(0).DisplayName + "\n\n" +
							mostRecentBookPercentRead + "% Completed");
					mostRecentBookSummary.setSpan(new RelativeSizeSpan(1.2f), 0, recentBookTitle.length(), 0);
					mostRecentBookTimeSpent = stats.getTotalTimeSpent();
					mostRecentBookPagesTurned = stats.getPagesTurned();
				} else {
					mostRecentBookSummary = new SpannableString("Start reading!");
				}
				break;
			} case completed: {
				booksCompleted = new ArrayList<Book>();
				for (BookQuery query = new BookQuery(booksCompletedFilter, 20); ; query = query.next()) {
					final List<Book> books = Collection.books(query);
					if (books.isEmpty()) {
						break;
					}
					booksCompleted.addAll(books);
				}
				booksReading = new ArrayList<Book>();
				for (BookQuery query = new BookQuery(booksReadingFilter, 20); ; query = query.next()) {
					final List<Book> books = Collection.books(query);
					if (books.isEmpty()) {
						break;
					}
					for (Book book : books) {
						numPagesTurned += book.getStatistics().getPagesTurned();
					}
					booksReading.addAll(books);
				}
				booksFavorited = new ArrayList<Book>();
				for (BookQuery query = new BookQuery(booksFavoritedFilter, 20); ; query = query.next()) {
					final List<Book> books = Collection.books(query);
					if (books.isEmpty()) {
						break;
					}
					booksFavorited.addAll(books);
				}
				break;
			} case reading: {
			} case average: {
			} case library: {
			} default: {
			}
		}
	}

	@Override
	public boolean hasUniqueView() { return true; }

	@Override
	public Status getOpeningStatus() {
		return isSelectable ? Status.ALWAYS_RELOAD_BEFORE_OPENING : Status.WAIT_FOR_OPEN;
	}

	@Override
	public String getName() {
		return entryTitle;
	}

	// pseudo getSummary()
	public SpannableString getSpannableSummary() {
		return mostRecentBookSummary;
	}

	// not used, see getSpannableSummary()
	@Override
	public String getSummary() { return getSpannableSummary().toString(); }

	@Override
	public Book getBook() {	return mostRecentBook; }

	@Override
	public ZLImage createCover() { return BookUtil.getCover(mostRecentBook); }

	@Override
	protected String getStringId() {
		return ROOT_STATISTICS + (mostRecentBook == null ? entryTitle : mostRecentBook.getTitle());
	}

	@Override
	public View onCreateUniqueView(View convertView, ViewGroup parent, LibraryTree tree, TreeActivity activity) {
		switch(myType) {
			case progress: {
				return createProgressStatisticsView(convertView, parent, tree, activity);
			} case completed: {
				return createCompletedStatisticsView(convertView, parent, tree, activity);
			} case reading: {
				return createReadingStatisticsView(convertView, parent, tree, activity);
			} case average: {
				return createAverageStatisticsView(convertView, parent, tree, activity);
			} case library: {
				return createLibraryStatisticsView(convertView, parent, tree, activity);
			} default: {
				return createProgressStatisticsView(convertView, parent, tree, activity);
			}
		}
	}

	private View createProgressStatisticsView(View convertView, ViewGroup parent, LibraryTree tree, TreeActivity activity) {
		final View view = (convertView != null && convertView.getId() == R.id.statistics_tree_id) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.statistics_tree_progress, parent, false);
		final int entryHeight = parent.getMeasuredHeight() / 5 - 1;
		view.setMinimumHeight(entryHeight);

		final TextView nameView = ViewUtil.findTextView(view, R.id.statistics_tree_item_name);
		nameView.setText(tree.getName());

		final TextView summaryView = ViewUtil.findTextView(view, R.id.statistics_tree_item_summary);
		summaryView.setText(getSpannableSummary());
		view.measure(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		final int summaryHeight = summaryView.getMeasuredHeight();

		final TextView rightView = ViewUtil.findTextView(view, R.id.statistics_tree_item_right);
		int diff = mostRecentBookTimeSpent;
		int millis = diff % 1000; diff /= 1000;
		int seconds = diff % 60; diff /= 60;
		int minutes = diff % 60; diff /= 60;
		int hours = diff;
		SpannableString rightText = new SpannableString(
				hours + ":" + minutes + "\nTime Spent\n\n" +
				mostRecentBookPagesTurned + "\nPages Turned");
		int str1Length = String.valueOf(hours + ":" + minutes).length();
		int str2Length = String.valueOf(mostRecentBookPagesTurned).length();
		rightText.setSpan(new RelativeSizeSpan(headingSize), 0, str1Length, 0);
		rightText.setSpan(new RelativeSizeSpan(headingSize), 13 + str1Length, 13 + str1Length + str2Length, 0);
		rightView.setText(rightText);

		final ImageView coverView = ViewUtil.findImageView(view, R.id.statistics_tree_item_cover);
		if (sharedCoverManager == null) {
			Log.d("manager", "manager created from progress");
			sharedCoverManager = new CoverManager(activity, activity.ImageSynchronizer, summaryHeight * 21 / 32 *7/5, summaryHeight*7/5);
			view.requestLayout();
		}
		if (!sharedCoverManager.trySetCoverImage(coverView, tree)) {
			coverView.setImageResource(R.drawable.ic_list_library_book);
		}

		final ProgressBar bar = (ProgressBar)ViewUtil.findView(view, R.id.statistics_tree_item_progress);
		bar.setProgress(mostRecentBookPercentRead);
		return view;
	}

	private View createCompletedStatisticsView(View convertView, ViewGroup parent, LibraryTree tree, TreeActivity activity) {
		final View view = (convertView != null && convertView.getId() == R.id.statistics_tree_id) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.statistics_tree_completed, parent, false);
		final int entryHeight = parent.getMeasuredHeight() / 5 - 1;
		view.setMinimumHeight(entryHeight);

		final TextView nameView = ViewUtil.findTextView(view, R.id.statistics_tree_item_name);
		nameView.setText(tree.getName());

		final TextView leftPanelView = ViewUtil.findTextView(view, R.id.statistics_tree_completed_left);
		final String str1 = String.valueOf(booksCompleted.size());
		final String str2 = String.valueOf(numPagesTurned);
		SpannableString leftPanelText = new SpannableString(
				str1 + "\nBooks Completed\n\n" +
				str2 + "\nPages Turned");
		leftPanelText.setSpan(new RelativeSizeSpan(headingSize), 0, str1.length(), 0);
		leftPanelText.setSpan(new RelativeSizeSpan(headingSize), 18 + str1.length(), 18 + str1.length() + str2.length(), 0);
		leftPanelView.setText(leftPanelText);

		return view;
	}

	private View createReadingStatisticsView(View convertView, ViewGroup parent, LibraryTree tree, TreeActivity activity) {
		final View view = (convertView != null && convertView.getId() == R.id.statistics_tree_id) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.statistics_tree_completed, parent, false);
		final int entryHeight = parent.getMeasuredHeight() / 5 - 1;
		view.setMinimumHeight(entryHeight);

		final TextView nameView = ViewUtil.findTextView(view, R.id.statistics_tree_item_name);
		nameView.setText(tree.getName());

		return view;
	}

	private View createAverageStatisticsView(View convertView, ViewGroup parent, LibraryTree tree, TreeActivity activity) {
		final View view = (convertView != null && convertView.getId() == R.id.statistics_tree_id) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.statistics_tree_average, parent, false);
		final int entryHeight = parent.getMeasuredHeight() / 5 - 1;
		view.setMinimumHeight(entryHeight);

		final TextView nameView = ViewUtil.findTextView(view, R.id.statistics_tree_item_name);
		nameView.setText(tree.getName());

		return view;
	}

	private View createLibraryStatisticsView(View convertView, ViewGroup parent, LibraryTree tree, TreeActivity activity) {
		final View view = (convertView != null && convertView.getId() == R.id.statistics_tree_id) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.statistics_tree_completed, parent, false);
		final int entryHeight = parent.getMeasuredHeight() / 5 - 1;
		view.setMinimumHeight(entryHeight);

		final TextView nameView = ViewUtil.findTextView(view, R.id.statistics_tree_item_name);
		nameView.setText(tree.getName());

		return view;
	}

	// if statistics page contains mentioned book
	@Override
	public boolean containsBook(Book book) {
		Log.d("checkUsed", "containsbook");
		return book != null && book.equals(mostRecentBook);
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
