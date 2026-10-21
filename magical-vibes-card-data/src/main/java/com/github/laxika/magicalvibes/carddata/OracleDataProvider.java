package com.github.laxika.magicalvibes.carddata;

/**
 * The upstream services a set can be downloaded from, and the accepted values of the
 * {@code oracle.data-provider} application property (matched case-insensitively). {@link #SCRYFALL}
 * is the default when the property is absent.
 *
 * <p>The property names the <em>preferred</em> provider: a set missing from the cache is downloaded
 * from it, and from the other one only if it still fails after retries. Either way the result is
 * converted and stored once — see {@link com.github.laxika.magicalvibes.carddata.store.SetDataStore} —
 * so a fallback cannot make oracle text differ from one run to the next.
 */
public enum OracleDataProvider {
    SCRYFALL,
    MTGJSON
}
