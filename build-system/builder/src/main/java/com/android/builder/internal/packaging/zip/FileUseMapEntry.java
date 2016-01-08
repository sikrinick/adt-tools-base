/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.builder.internal.packaging.zip;

import com.android.annotations.NonNull;
import com.android.annotations.Nullable;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.primitives.Ints;

import java.util.Comparator;

/**
 * Represents an entry in the {@link FileUseMap}. Each entry contains an interval of bytes. The
 * end of the interval is exclusive.
 * <p>
 * Entries can either be free or used. Used entries <em>must</em> store an object. Free entries
 * do not store anything.
 * <p>
 * File map entries are used to keep track of which parts of a file map are used and not.
 * @param <T> the type of data stored
 */
class FileUseMapEntry<T> {

    /**
     * Comparator that compares entries by their start date.
     */
    public static final Comparator<FileUseMapEntry<?>> COMPARE_BY_START =
            new Comparator<FileUseMapEntry<?>>() {
        @Override
        public int compare(@NonNull FileUseMapEntry<?> o1, @NonNull FileUseMapEntry<?> o2) {
            return Ints.checkedCast(o1.getStart() - o2.getStart());
        }
    };

    /**
     * The first byte in the entry.
     */
    private final long mStart;

    /**
     * The first byte no longer in the entry.
     */
    private final long mEnd;

    /**
     * The stored data. If {@code null} then this entry represents a free entry.
     */
    @Nullable
    private final T mStore;

    /**
     * Creates a new map entry.
     * @param start the start of the entry
     * @param end the end of the entry (first byte no longer in the entry)
     * @param store the data to store in the entry or {@code null} if this is a free entry
     */
    private FileUseMapEntry(long start, long end, @Nullable T store) {
        Preconditions.checkArgument(start >= 0, "start < 0");
        Preconditions.checkArgument(end > start, "end <= start");

        mStart = start;
        mEnd = end;
        mStore = store;
    }

    /**
     * Creates a new free entry.
     * @param start the start of the entry
     * @param end the end of the entry (first byte no longer in the entry)
     * @return the entry
     */
    public static FileUseMapEntry<Object> makeFree(long start, long end) {
        return new FileUseMapEntry<Object>(start, end, null);
    }

    /**
     * Creates a new used entry.
     * @param start the start of the entry
     * @param end the end of the entry (first byte no longer in the entry)
     * @param store the data to store in the entry
     * @param <T> the type of data to store in the entry
     * @return the entry
     */
    public static <T> FileUseMapEntry<T> makeUsed(long start, long end, @NonNull T store) {
        Preconditions.checkNotNull(store, "store == null");
        return new FileUseMapEntry<T>(start, end, store);
    }

    /**
     * Obtains the first byte in the entry.
     * @return the first byte in the entry (if the same value as {@link #getEnd()} then the entry
     * is empty and contains no data)
     */
    long getStart() {
        return mStart;
    }

    /**
     * Obtains the first byte no longer in the entry.
     * @return the first byte no longer in the entry
     */
    long getEnd() {
        return mEnd;
    }

    /**
     * Obtains the size of the entry.
     * @return the number of bytes contained in the entry
     */
    long getSize() {
        return mEnd - mStart;
    }

    /**
     * Determines if this is a free entry.
     * @return is this entry free?
     */
    boolean isFree() {
        return mStore == null;
    }

    /**
     * Obtains the data stored in the entry.
     * @return the data stored or {@code null} if this entry is a free entry
     */
    @Nullable
    T getStore() {
        return mStore;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("start", mStart)
                .add("end", mEnd)
                .add("store", mStore)
                .toString();
    }
}
