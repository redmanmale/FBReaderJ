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

public class StatisticsListTree extends FirstLevelTree {
	StatisticsListTree(RootTree root) {
		super(root, ROOT_STATISTICS);
	}

	public static final String PROGRESS_TITLE = "progressTitle";
	public static final String COMPLETED_TITLE = "completedTitle";
	public static final String READING_TIMES_TITLE = "readingTimesTitle";
	public static final String AVERAGE_TITLE = "averageTitle";
	public static final String LIBRARY_TITLE = "libraryTitle";

	@Override
	public Status getOpeningStatus() {
		return Status.ALWAYS_RELOAD_BEFORE_OPENING;
	}

	@Override
	public void waitForOpening() {
		clear();

		new StatisticsTree(this, PROGRESS_TITLE, "PROGRESS_TITLE");
		new StatisticsTree(this, COMPLETED_TITLE, "COMPLETED_TITLE");
		new StatisticsTree(this, READING_TIMES_TITLE, "READING_TIMES_TITLE");
		new StatisticsTree(this, AVERAGE_TITLE, "AVERAGE_TITLE");
		new StatisticsTree(this, LIBRARY_TITLE, "LIBRARY_TITLE");
	}
}
