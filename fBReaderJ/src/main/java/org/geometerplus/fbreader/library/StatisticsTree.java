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

import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.TabStopSpan;
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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	public static Map<String, List<Book>> seriesCompletionMap;
	public static int totalPagesTurned;
	public static int totalTimeSpent;
	// average
	public static int averageBookTimeSpent;
	public static int averagePagesPerHour;
	// library
	public static int numBooksInLibrary;
	public static int numSeriesInLibrary;
	public static int numAuthorsInLibrary;
	public static int numTagsInLibrary;
	public static List<Bookmark> boomarksInLibrary;
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

				seriesCompletionMap = new HashMap<String, List<Book>>();
				for (Book book : booksCompleted) {
					final SeriesInfo seriesInfo = book.getSeriesInfo();
					if (seriesInfo == null || seriesInfo.Series == null) {
						continue;
					}
					final String title = seriesInfo.Series.getTitle();
					if(seriesCompletionMap.containsKey(title)) {
						seriesCompletionMap.get(title).add(book);
						Log.d("check9", "Added book " + book.getTitle() + " into ArrayList at " + title);
					} else {
						Log.d("check9", "Created ArrayList for: " + title);
						seriesCompletionMap.put(title, new ArrayList<Book>());
						seriesCompletionMap.get(title).add(book);
					}
				}
				final ArrayList<String> seriesToBeRemoved = new ArrayList<String>();
				for (String title : seriesCompletionMap.keySet()) {
					Log.d("check9", "On keySet title: " + title);
					final Filter seriesFilter = new Filter.BySeries(new Series(title));
					List<Book> allBooksInThisSeries = new ArrayList<Book>();
					for (BookQuery query = new BookQuery(seriesFilter, 20); ; query = query.next()) {
						final List<Book> books = Collection.books(query);
						if (books.isEmpty()) {
							break;
						}
						allBooksInThisSeries.addAll(books);
					}
					if (seriesCompletionMap.get(title).size() < allBooksInThisSeries.size()) {
						Log.d("check9", "Marked for deletion: " + title);
						seriesToBeRemoved.add(title);
					}
				}
				for (String seriesToRemove : seriesToBeRemoved) {
					Log.d("check9", "Removed: " + seriesToRemove);
					seriesCompletionMap.remove(seriesToRemove);
				}

				booksReading = new ArrayList<Book>();
				totalPagesTurned = 0;
				totalTimeSpent = 0;
				for (BookQuery query = new BookQuery(booksReadingFilter, 20); ; query = query.next()) {
					final List<Book> books = Collection.books(query);
					if (books.isEmpty()) {
						break;
					}
					for (Book book : books) {
						BookStatistics stats = book.getStatistics();
						totalPagesTurned += stats.getPagesTurned();
						totalTimeSpent += stats.getTotalTimeSpent();
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
				averageBookTimeSpent = 0;
				averagePagesPerHour = 0;
				for (Book book : booksReading) {
					int timeSpent = book.getStatistics().getTotalTimeSpent();
					averageBookTimeSpent += timeSpent;
					int hours = (timeSpent / (1000 * 60 * 60)) % 24;
					averagePagesPerHour += book.getStatistics().getPagesTurned() / (hours == 0 ? 1 : hours);
				}
				averageBookTimeSpent /= booksReading.size();
			} case library: {
				numSeriesInLibrary = Collection.series().size();
				numAuthorsInLibrary = Collection.authors().size();
				numTagsInLibrary = Collection.tags().size();
				numBooksInLibrary = Collection.size();
				boomarksInLibrary = new ArrayList<Bookmark>();
				for (BookmarkQuery query = new BookmarkQuery(20); ; query = query.next()) {
					final List<Bookmark> bookmarks = Collection.bookmarks(query);
					if (bookmarks.isEmpty()) {
						break;
					}
					boomarksInLibrary.addAll(bookmarks);
				}
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
		int temp = mostRecentBookTimeSpent / 1000;
		final int seconds = temp % 60; temp /= 60;
		final int minutes = temp % 60; temp /= 60;
		final int hours = temp;
		final String str1 = String.format("%02d:%02d:%02d", hours, minutes, seconds);
		final String str2 = String.valueOf(mostRecentBookPagesTurned);
		final SpannableString rightText = new SpannableString(
				str1 + "\nTime Spent (h:m:s)\n\n" + // 21
				str2 + "\nPages Turned");
		rightText.setSpan(new RelativeSizeSpan(headingSize), 0, str1.length(), 0);
		rightText.setSpan(new RelativeSizeSpan(headingSize), 21 + str1.length(), 21 + str1.length() + str2.length(), 0);
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
		final String lpStr1 = String.valueOf(booksCompleted.size());
		final String lpStr2 = String.valueOf(seriesCompletionMap.size());
		final SpannableString leftPanelText = new SpannableString(
				lpStr1 + "\nBooks\n\n" +
				lpStr2 + "\nSeries");
		leftPanelText.setSpan(new RelativeSizeSpan(headingSize), 0, lpStr1.length(), 0);
		leftPanelText.setSpan(new RelativeSizeSpan(headingSize), 8 + lpStr1.length(), 8 + lpStr1.length() + lpStr2.length(), 0);
		leftPanelView.setText(leftPanelText);

		final TextView centerPanelView = ViewUtil.findTextView(view, R.id.statistics_tree_completed_center);
		int temp = totalTimeSpent / 1000 / 60;
		final int minutes = temp % 60; temp /= 60;
		final int hours = temp % 24; temp /= 24;
		final int days = temp;
		/*final Date timeSpent = new Date(totalTimeSpent);
		final String cpStr1 = String.valueOf(String.format("%02d:%02d:%02d", timeSpent.getHours() / 24, timeSpent.getHours() % 24, timeSpent.getMinutes()));*/
		final String cpStr1 = String.valueOf(String.format("%02d:%02d:%02d", days, hours, minutes));
		final String cpStr2 = String.valueOf("");
		final SpannableString centerPanelText = new SpannableString(
				cpStr1 + "\nTotal Time Spent\n(d:h:m)\n\n" +
				cpStr2 + "\n");
		centerPanelText.setSpan(new RelativeSizeSpan(headingSize), 0, cpStr1.length(), 0);
		centerPanelText.setSpan(new RelativeSizeSpan(headingSize), 27 + cpStr1.length(), 27 + cpStr1.length() + cpStr2.length(), 0);
		centerPanelView.setText(centerPanelText);

		final TextView rightPanelView = ViewUtil.findTextView(view, R.id.statistics_tree_completed_right);
		final int percent = (int)(RationalNumber.create(booksCompleted.size(), numBooksInLibrary).toFloat() * 100);
		final String rpStr1 = String.valueOf(percent) + "%";
		final String rpStr2 = String.valueOf(totalPagesTurned);
		final SpannableString rightPanelText = new SpannableString(
				rpStr1 + "\nOf Your Library\n\n" + //18
				rpStr2 + "\nTotal Pages Turned");
		rightPanelText.setSpan(new RelativeSizeSpan(headingSize), 0, rpStr1.length(), 0);
		rightPanelText.setSpan(new RelativeSizeSpan(headingSize), 18 + rpStr1.length(), 18 + rpStr1.length() + rpStr2.length(), 0);
		rightPanelView.setText(rightPanelText);

		return view;
	}

	private View createReadingStatisticsView(View convertView, ViewGroup parent, LibraryTree tree, TreeActivity activity) {
		final View view = (convertView != null && convertView.getId() == R.id.statistics_tree_id) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.statistics_tree_reading, parent, false);
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

		final TextView leftPanelView = ViewUtil.findTextView(view, R.id.statistics_tree_average_left);
		final String lpStr1 = String.valueOf(averagePagesPerHour);
		final String lpStr2 = String.valueOf(averagePagesPerHour);
		final SpannableString leftPanelText = new SpannableString(
				lpStr1 + "\nPages Per Hour\n\n" + // 17
				lpStr2 + "\nPages Per Hour");
		leftPanelText.setSpan(new RelativeSizeSpan(headingSize), 0, lpStr1.length(), 0);
		leftPanelText.setSpan(new RelativeSizeSpan(headingSize), 17 + lpStr1.length(), 17 + lpStr1.length() + lpStr2.length(), 0);
		leftPanelView.setText(leftPanelText);


		final TextView centerPanelView = ViewUtil.findTextView(view, R.id.statistics_tree_average_center);
		int temp = averageBookTimeSpent / 1000;
		final int seconds = temp % 60; temp /= 60;
		final int minutes = temp % 60; temp /= 60;
		final int hours = temp;
		final String cpStr1 = String.format("%02d:%02d:%02d", hours, minutes, seconds);
		final String cpStr2 = String.valueOf("");
		final SpannableString centerPanelText = new SpannableString(
				cpStr1 + "\nTime Per Book\n(h:m:s)\n\n" + // 25
				cpStr2 + "\n");
		centerPanelText.setSpan(new RelativeSizeSpan(headingSize), 0, cpStr1.length(), 0);
		centerPanelText.setSpan(new RelativeSizeSpan(headingSize), 25 + cpStr1.length(), 25 + cpStr1.length() + cpStr2.length(), 0);
		centerPanelView.setText(centerPanelText);

		return view;
	}

	private View createLibraryStatisticsView(View convertView, ViewGroup parent, LibraryTree tree, TreeActivity activity) {
		final View view = (convertView != null && convertView.getId() == R.id.statistics_tree_id) ? convertView :
				LayoutInflater.from(parent.getContext()).inflate(R.layout.statistics_tree_library, parent, false);
		final int entryHeight = parent.getMeasuredHeight() / 5 - 1;
		view.setMinimumHeight(entryHeight);

		final TextView nameView = ViewUtil.findTextView(view, R.id.statistics_tree_item_name);
		nameView.setText(tree.getName());

		final TextView leftPanelView = ViewUtil.findTextView(view, R.id.statistics_tree_left);
		final String lpStr1 = String.valueOf(numSeriesInLibrary);
		final String lpStr2 = String.valueOf(boomarksInLibrary.size());
		final SpannableString leftPanelText = new SpannableString(
				lpStr1 + "\nSeries\n\n" + // 9
				lpStr2 + "\nBookmarks");
		leftPanelText.setSpan(new RelativeSizeSpan(headingSize), 0, lpStr1.length(), 0);
		leftPanelText.setSpan(new RelativeSizeSpan(headingSize), 9 + lpStr1.length(), 9 + lpStr1.length() + lpStr2.length(), 0);
		leftPanelView.setText(leftPanelText);

		final TextView centerPanelViewL = ViewUtil.findTextView(view, R.id.statistics_tree_centerL);
		final String cpStrL = String.valueOf(numBooksInLibrary);
		final SpannableString centerPanelTextL = new SpannableString(
				cpStrL + "\n" + "Books");
		centerPanelTextL.setSpan(new RelativeSizeSpan(headingSize), 0, cpStrL.length(), 0);
		centerPanelViewL.setText(centerPanelTextL);

		final TextView centerPanelViewR = ViewUtil.findTextView(view, R.id.statistics_tree_centerR);
		final String cpStrR = String.valueOf(numAuthorsInLibrary);
		final SpannableString centerPanelTextR = new SpannableString(
				cpStrR + "\n" + "Authors");
		centerPanelTextR.setSpan(new RelativeSizeSpan(headingSize), 0, cpStrR.length(), 0);
		centerPanelViewR.setText(centerPanelTextR);

		final TextView rightPanelView = ViewUtil.findTextView(view, R.id.statistics_tree_right);
		final String rpStr1 = String.valueOf(numTagsInLibrary);
		final String rpStr2 = String.valueOf(booksFavorited.size());
		final SpannableString rightPanelText = new SpannableString(
				rpStr1 + "\nTags\n\n" + // 7
				rpStr2 + "\nFavorites");
		rightPanelText.setSpan(new RelativeSizeSpan(headingSize), 0, rpStr1.length(), 0);
		rightPanelText.setSpan(new RelativeSizeSpan(headingSize), 7 + rpStr1.length(), 7 + rpStr1.length() + rpStr2.length(), 0);
		rightPanelView.setText(rightPanelText);

		return view;
	}

	/*
	@Override
	public boolean onBookEvent(BookEvent event, Book book) {
		switch (event) {
			case Added: {
				return true;
			} case Updated: {
				return true;
			} case Removed: {
				return true;
			} default: {
				return super.onBookEvent(event, book);
			}
		} */


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
