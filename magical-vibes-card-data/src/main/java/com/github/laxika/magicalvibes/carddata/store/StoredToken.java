package com.github.laxika.magicalvibes.carddata.store;

import com.github.laxika.magicalvibes.model.OracleData;

/**
 * One entry of a set's token list.
 *
 * <p>Kept whole rather than reduced to an image key: a set may register a token class under its
 * own code at a token's collector number, and that class needs the token's oracle data.
 *
 * @param face     the token's oracle data, mapped when the set was converted
 * @param imageKey the {@link com.github.laxika.magicalvibes.carddata.CardPrintingRegistry#buildTokenKey}
 *                 key this entry's art is found under; null for an emblem and for every face of a
 *                 double-faced token but its front, which supply no token art
 */
public record StoredToken(String collectorNumber, OracleData face, String imageKey) {
}
