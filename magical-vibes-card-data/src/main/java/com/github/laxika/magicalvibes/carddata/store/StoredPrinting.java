package com.github.laxika.magicalvibes.carddata.store;

import com.github.laxika.magicalvibes.model.OracleData;

import java.util.List;

/**
 * One printing of a {@link StoredSet}, its faces already mapped by
 * {@link com.github.laxika.magicalvibes.carddata.FaceOracleMapper} when the set was converted.
 *
 * @param rarity    lower-case rarity ("common", "mythic", ...), or null when the provider has none
 * @param faceNames every printed face name in printed order, used to validate the class name of a
 *                  split card with more than two faces
 * @param front     the printing's front face; for a split card, the whole card with its halves
 * @param back      the face {@code card_faces[1]} / side "b" describes, or null when there is none
 */
public record StoredPrinting(
        String collectorNumber,
        String rarity,
        List<String> faceNames,
        OracleData front,
        OracleData back) {
}
