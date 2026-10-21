package com.github.laxika.magicalvibes.carddata;

import java.util.Set;

/**
 * Reads oracle data for one set.
 *
 * <p>Loaders do not run themselves and write nothing: {@link CardRegistry} calls this once per set
 * and performs every registration from the result. The one implementation,
 * {@link com.github.laxika.magicalvibes.carddata.store.StoredSetOracleLoader}, reads the server's
 * stored set files; the interface exists so registry tests can supply sets without any files.
 */
public interface OracleLoader {
    /** Every printing's format legality, as stored with the set. */
    default LegalitySnapshot loadLegalities(String setCode) { return LegalitySnapshot.empty(); }

    /**
     * @param setCode                     the set to read, e.g. {@code "ISD"}
     * @param implementedCollectorNumbers which printings the game actually implements. Only these
     *                                    need their oracle text parsed — a set is mostly cards with
     *                                    no class behind them, and parsing all of them would be
     *                                    work thrown away. Set-wide fields
     *                                    ({@link SetOracleData#cardTotal},
     *                                    {@link SetOracleData#rarityByCollectorNumber}) still cover
     *                                    the whole set.
     * @throws RuntimeException if the set is not stored and no provider could supply it
     */
    SetOracleData loadSet(String setCode, Set<String> implementedCollectorNumbers);
}
