package com.github.laxika.magicalvibes.carddata.store;

import com.github.laxika.magicalvibes.model.OracleData;

/**
 * What the card browser shows of one face beyond the face's oracle data in the {@link StoredSet}.
 *
 * @param browse    the face as the browser shows it — one face at a time, so a name, cost and type
 *                  line spanning both halves is cut to the first before mapping. Null when that is
 *                  no different from the face's oracle data, which it is for every card but a split
 *                  one.
 * @param typeLine  the printed type line, cut to its first face; shown, never parsed
 * @param power     the printed power ("4", "*", "1+*"), or null; shown, never parsed
 * @param toughness the printed toughness, or null; shown, never parsed
 */
public record StoredFaceDetails(
        OracleData browse,
        String typeLine,
        String power,
        String toughness) {

    /** The face as the card browser shows it, given the face's oracle data in the {@link StoredSet}. */
    public OracleData browseOracle(OracleData oracle) {
        return browse != null ? browse : oracle;
    }
}
