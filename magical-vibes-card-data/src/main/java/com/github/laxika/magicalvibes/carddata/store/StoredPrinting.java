package com.github.laxika.magicalvibes.carddata.store;

import com.github.laxika.magicalvibes.carddata.RawFace;

import java.util.List;
import java.util.Map;

/**
 * One printing of a {@link StoredSet}.
 *
 * @param rarity      lower-case rarity ("common", "mythic", ...), or null when the provider has none
 * @param faceNames   every printed face name in printed order, used to validate the class name of a
 *                    split card with more than two faces
 * @param front       the printing's front face; for a split card, the whole card
 * @param back        the face {@code card_faces[1]} / side "b" describes, or null when there is none
 * @param splitHalves each half of a split or aftermath card in printed order; empty for every other
 *                    layout
 * @param legalities  format name to legality status, both lower case
 */
public record StoredPrinting(
        String collectorNumber,
        String rarity,
        List<String> faceNames,
        RawFace front,
        RawFace back,
        List<RawFace> splitHalves,
        PrintVariant variant,
        Map<String, String> legalities) {
}
