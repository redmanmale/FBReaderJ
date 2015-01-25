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

package org.geometerplus.fbreader.book;

import android.util.Log;

public final class BookStatistics {
	private final long myBookID;
	private final long myDateAdded;
	private long myDateOpened;
	private long myDateClosed;
	private int myPagesTurned;
	private int myTotalTimeSpent;

	public BookStatistics(long book_id, long date_added, long date_opened,
						  long date_closed, int pages_turned, int total_time_spent) {
		myBookID = book_id;
		myDateAdded = date_added;
		myDateOpened = date_opened;
		myDateClosed = date_closed;
		myPagesTurned = pages_turned;
		myTotalTimeSpent = total_time_spent;
	}

	public BookStatistics(long book_id) {
		this(book_id, System.currentTimeMillis(), -1, -1, 0, 0);
	}

	public void startSession(long date_opened) {
		Log.d("check4", "startSession: " + date_opened + "\tID: " + System.identityHashCode(this) + "\n" + toString());
		myDateOpened = date_opened;
	}

	public void endSession(long date_closed) {
		myDateClosed = date_closed;
		int elapsed = (int) (myDateClosed - myDateOpened);
		Log.d("check4", "endSession: " + date_closed + "\tID: " + System.identityHashCode(this) + "\n" + toString() + " \t " + elapsed);
		myTotalTimeSpent += elapsed > 0 ? elapsed : 0;
	}

	public void incrementPagesTurned() {
		this.myPagesTurned++;
	}

	public long getBookID() {
		return myBookID;
	}

	public long getDateAdded() {
		return myDateAdded;
	}

	public long getDateOpened() {
		return myDateOpened;
	}

	public long getDateClosed() {
		return myDateClosed;
	}

	public int getPagesTurned() {
		return myPagesTurned;
	}

	public int getTotalTimeSpent() {
		return myTotalTimeSpent;
	}

	public String toString() {
		return "book_id: " + myBookID +
		"\tdate_added: " + myDateAdded +
		"\tdate_opened: " + myDateOpened +
		"\tdate_closed: " + myDateClosed +
		"\tpages_turned: " + myPagesTurned +
		"\ttotal_time_spent: " + myTotalTimeSpent;
	}
}
