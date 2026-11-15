package com.github.laxika.magicalvibes.carddata.store;

import com.github.laxika.magicalvibes.carddata.OracleDataProvider;

import java.util.List;

/**
 * One set in the server's own provider-neutral format, cut down to what loading it reads: the
 * contents of {@code card-data-cache/set-{code}.cbor}. What only the card browser and deck
 * validation read is the {@link StoredSetDetails} beside it.
 *
 * <p>The provider's download is converted into this once and then thrown away, so nothing
 * downstream ever reads Scryfall or MTGJSON JSON. Faces are kept <em>after</em>
 * {@link com.github.laxika.magicalvibes.carddata.FaceOracleMapper} has interpreted them, so loading
 * a set maps nothing. The price is that a file holds the mapping of the day it was written:
 * {@link #CONVERSION_VERSION} is what tells a file written by an older mapping from a current one.
 *
 * <p>Every printing's faces are kept, not only the implemented ones: which printings are
 * implemented changes with the code, and a file that left the others out would have to be
 * downloaded again for each newly implemented card.
 *
 * <p>A stored set is refreshed only by a new {@link #CONVERSION_VERSION} or by deleting its file.
 *
 * @param conversionVersion {@link #CONVERSION_VERSION} at the time of writing; a file with any
 *                      other value is treated as missing and downloaded again
 * @param setCode       the set code the file was requested for, which is also its file name — MB1
 *                      is stored as {@code set-mb1.cbor} although its data comes from CMB1
 * @param source        which provider the data was converted from. Recorded because a set whose
 *                      preferred provider was down on its first fetch comes from the other one.
 * @param sourceDate    the provider's own build date (MTGJSON {@code meta.date}), or null
 * @param tokenSetCode  the Scryfall token set code ({@code t} + source set code) the token
 *                      printings' images are served under
 * @param printings     every printing in the set, implemented or not
 * @param tokens        every token printing in the set's token list, emblems included
 */
public record StoredSet(
        int conversionVersion,
        String setCode,
        String setName,
        OracleDataProvider source,
        String sourceDate,
        String tokenSetCode,
        List<StoredPrinting> printings,
        List<StoredToken> tokens) {

    /**
     * Bump whenever conversion would write a set differently, so existing files are downloaded
     * again instead of read. That is any change to how a {@link SetSource} flattens its provider's
     * JSON, to {@link com.github.laxika.magicalvibes.carddata.FaceOracleMapper},
     * {@link com.github.laxika.magicalvibes.carddata.TypeLineParser},
     * {@link com.github.laxika.magicalvibes.carddata.OracleTextNormalizer} or the maps and parsers of
     * {@link com.github.laxika.magicalvibes.carddata.CardDataSupport}, to {@link StoredSetConverter},
     * and any constant added to or renamed in the enums the mapped faces are made of
     * ({@code Keyword}, {@code CardSubtype}, {@code CardType}, {@code CardSupertype}): a stored
     * face that printed the new keyword or subtype was written without it.
     *
     * <p>The CI cache key for {@code card-data-cache} hashes this file, so it changes with it.
     */
    public static final int CONVERSION_VERSION = 3;
}
