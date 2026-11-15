package com.github.laxika.magicalvibes.carddata.store;

/** One printing as {@link StoredSetConverter} converts it: a part for each of the set's two files. */
public record ConvertedPrinting(StoredPrinting printing, StoredPrintingDetails details) {
}
