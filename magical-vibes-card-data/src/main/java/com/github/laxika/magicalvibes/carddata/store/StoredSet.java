package com.github.laxika.magicalvibes.carddata.store;

import com.github.laxika.magicalvibes.carddata.OracleDataProvider;

import java.time.Instant;
import java.util.List;

/**
 * One set in the server's own provider-neutral format: the contents of
 * {@code card-data-cache/set-{code}.json}.
 *
 * <p>The provider's download is converted into this once and then thrown away, so nothing
 * downstream ever reads Scryfall or MTGJSON JSON. Faces are kept as {@link com.github.laxika.magicalvibes.carddata.RawFace}s, i.e.
 * <em>before</em> {@link com.github.laxika.magicalvibes.carddata.FaceOracleMapper} interprets them,
 * so a rules-mapping fix takes effect on the next start without a re-download. Only a change to how
 * a source flattens its provider's JSON needs fresh data, and that is what bumping
 * {@link #FORMAT_VERSION} is for.
 *
 * <p>Nothing ever refreshes a stored set: deleting its file is the only way to re-download it.
 *
 * @param formatVersion {@link #FORMAT_VERSION} at the time of writing; a file with any other value
 *                      is treated as missing
 * @param setCode       the set code the file was requested for, which is also its file name — MB1
 *                      is stored as {@code set-mb1.json} although its data comes from CMB1
 * @param source        which provider the data was converted from. Recorded because a set whose
 *                      preferred provider was down on its first fetch comes from the other one.
 * @param sourceDate    the provider's own build date (MTGJSON {@code meta.date}), or null
 * @param fetchedAt     when the set was downloaded; also the date deck validation shows for its
 *                      legality data
 * @param tokenSetCode  the Scryfall token set code ({@code t} + source set code) the token
 *                      printings' images are served under
 * @param printings     every printing in the set, implemented or not
 * @param tokens        every token printing in the set's token list, emblems included
 */
public record StoredSet(
        int formatVersion,
        String setCode,
        String setName,
        OracleDataProvider source,
        String sourceDate,
        Instant fetchedAt,
        String tokenSetCode,
        List<StoredPrinting> printings,
        List<StoredToken> tokens) {

    /**
     * Bump whenever a source's conversion changes what it writes, so existing files are
     * re-downloaded instead of read. The CI cache key for {@code card-data-cache} has to change
     * with it.
     */
    public static final int FORMAT_VERSION = 2;
}
