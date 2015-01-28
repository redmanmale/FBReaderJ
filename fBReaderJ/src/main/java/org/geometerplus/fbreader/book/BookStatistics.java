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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class BookStatistics {
	private final long myBookID;
	private final long myDateAdded;
	private long myDateOpened;
	private String mySessions;
	private int myPagesTurned;
	private int myTotalTimeSpent;

	public final int SESSION_SIZE = 350;
	public final int SESSION_RESERVED = 4;

	public BookStatistics(long book_id, long date_added, long date_opened,
						  String sessions, int pages_turned, int total_time_spent) {
		myBookID = book_id;
		myDateAdded = date_added;
		myDateOpened = date_opened;
		mySessions = sessions;
		myPagesTurned = pages_turned;
		myTotalTimeSpent = total_time_spent;
	}

	public BookStatistics(long book_id) {
		this(book_id, System.currentTimeMillis(), -1, "", 0, 0);
	}

	public void startSession(long date_opened) {
		Log.d("check4", "startSession: " + date_opened + "\tID: " + System.identityHashCode(this) + "\n" + toString());
		myDateOpened = date_opened;
	}

	public void endSession(long date_closed) {
		int elapsedMS = (int) (date_closed - myDateOpened);
		// endSession() is called twice (once from FBReader.onPause(), and again from
		// ZLApplication.closeWindow(), so ignore the second call
		if(elapsedMS <= 0)
			return;
		// Intervals longer than 10 seconds qualify as a reading session
		if(elapsedMS / 1000 > 10) {
			// Last SESSION_RESERVED characters used to count total number of sessions
			final String reserved = String.format("%0" + SESSION_RESERVED + "d", getNumberOfSessions() + 1);
			// Junk last SESSION_RESERVED characters
			mySessions = mySessions.substring(0, Math.max(0, mySessions.length() - SESSION_RESERVED));
			// The date stored is accurate to minutes, session length is accurate to seconds
			// The session date's time of epoch is the book's added date
			final String newSession = String.valueOf((myDateOpened - myDateAdded) / 1000 / 60) + "," + (elapsedMS / 1000);
			mySessions = newSession + (mySessions.length() == 0 ? "" : "," + mySessions);
			// Throw away oldest sessions if needed to keep within SESSION_SIZE
			mySessions = mySessions.substring(0, Math.min(mySessions.length(), SESSION_SIZE - SESSION_RESERVED)) + reserved;
		}
		Log.d("check4", "endSession: " + date_closed + "\tID: " + System.identityHashCode(this) + " \t elapsed: " + elapsedMS + "\n" + toString());
		myDateOpened = date_closed;
		myTotalTimeSpent += elapsedMS;
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

	public String getSesssions() {
		return mySessions;
	}

	public int getNumberOfSessions() {
		final int len = mySessions.length();
		return len < SESSION_RESERVED ? 0 : Integer.valueOf(mySessions.substring(len - SESSION_RESERVED, len));
	}

	public Map<Long, Integer> getProcessedSessions() {
		if(mySessions.equals("")) return null;
		// Junk last SESSION_RESERVED characters
		final String sessions = mySessions.substring(0, Math.max(0, mySessions.length() - SESSION_RESERVED));
		String[] parts = sessions.split(",");
		// Junk last CSV if odd
		final int max = parts.length % 2 == 0 ? parts.length : parts.length - 1;
		Map<Long, Integer> dateSessionMap = new HashMap<Long, Integer>();
		for(int i = 0; i < max; i += 2)
			dateSessionMap.put(Integer.valueOf(parts[i]) * 60 * 1000 + myDateAdded, Integer.valueOf(parts[i + 1]));
		return dateSessionMap;
	}

	public int getPagesTurned() {
		return myPagesTurned;
	}

	public int getTotalTimeSpent() {
		return myTotalTimeSpent;
	}

	public String toString() {
		return "book_id: " + myBookID +
		"\tdate_added: " + new Date(myDateAdded).toLocaleString() +
		"\tdate_opened: " + new Date(myDateOpened).toLocaleString() +
		"\nsessions:\t" + mySessions +
		"\nsessions.length(): " + mySessions.length() +
		"\npages_turned: " + myPagesTurned +
		"\ttotal_time_spent: " + myTotalTimeSpent;
	}
}
