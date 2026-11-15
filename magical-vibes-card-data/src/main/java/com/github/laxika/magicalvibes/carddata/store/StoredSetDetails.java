package com.github.laxika.magicalvibes.carddata.store;

import java.time.Instant;
import java.util.List;

/**
 * The part of a converted set that loading it never reads: the contents of
 * {@code card-data-cache/set-{code}.details.cbor}. The card browser and deck validation read it,
 * on demand; it is kept out of the {@link StoredSet} so that a set load does not parse it.
 *
 * <p>Written together with the {@link StoredSet} of the same download, with the same printings in
 * the same order.
 *
 * @param conversionVersion {@link StoredSet#CONVERSION_VERSION} at the time of writing; a file with
 *                          any other value is treated as missing and the set is downloaded again
 * @param fetchedAt         when the set was downloaded; also the date deck validation shows for
 *                          its legality data
 * @param printings         every printing in the set, implemented or not
 */
public record StoredSetDetails(
        int conversionVersion,
        String setCode,
        Instant fetchedAt,
        List<StoredPrintingDetails> printings) {
}
