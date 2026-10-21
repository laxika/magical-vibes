package com.github.laxika.magicalvibes.carddata.store;

import com.github.laxika.magicalvibes.carddata.RawFace;

/**
 * One entry of a set's token list.
 *
 * <p>Kept whole rather than reduced to an image key: a set may register a token class under its
 * own code at a token's collector number, and that class needs the token's oracle data.
 *
 * @param side the face this entry is ("a", "b"), or null for a single-faced token
 */
public record StoredToken(String collectorNumber, String side, RawFace face) {
}
