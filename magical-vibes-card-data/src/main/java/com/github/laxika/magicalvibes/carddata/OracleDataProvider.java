package com.github.laxika.magicalvibes.carddata;

/**
 * Which upstream service the oracle registry is loaded from on startup. Selected via the
 * {@code oracle.data-provider} application property. With {@link #SCRYFALL}, MTGJSON is used
 * as an automatic fallback when Scryfall is unreachable.
 */
public enum OracleDataProvider {
    SCRYFALL,
    MTGJSON
}
