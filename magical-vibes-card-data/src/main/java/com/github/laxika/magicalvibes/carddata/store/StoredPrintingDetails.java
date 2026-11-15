package com.github.laxika.magicalvibes.carddata.store;

import java.util.Map;

/**
 * One printing of a {@link StoredSetDetails}: what the card browser and deck validation read of
 * it, and loading a set does not.
 *
 * @param layout     the provider's layout name ("normal", "transform", "split", "prepare", ...), or
 *                   null when it gave none
 * @param back       details of the printing's back face, or null when it has none
 * @param legalities format name to legality status, both lower case
 */
public record StoredPrintingDetails(
        String collectorNumber,
        String layout,
        PrintVariant variant,
        StoredFaceDetails front,
        StoredFaceDetails back,
        Map<String, String> legalities) {
}
