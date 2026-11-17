package com.github.laxika.magicalvibes.cards;

import com.github.laxika.magicalvibes.model.OracleData;

/**
 * An implemented card class as a catalog-wide search sees it: its first printing, and the oracle
 * data a card built from that printing would carry, read without building one.
 *
 * @param oracle   the data of the card, or of its front face
 * @param backFace the data of its back face, or null when it has no back face or the back face has
 *                 no oracle data
 */
public record CatalogEntry(CardPrinting printing, OracleData oracle, OracleData backFace) {
}
