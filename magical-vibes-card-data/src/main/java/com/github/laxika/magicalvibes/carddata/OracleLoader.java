package com.github.laxika.magicalvibes.carddata;

import java.util.Set;

/**
 * Reads oracle data for one set from an upstream source.
 *
 * <p>Exactly one implementation is a bean at a time, selected by the {@code oracle.data-provider}
 * property via {@code @ConditionalOnProperty} — see {@link OracleDataProvider} for the accepted
 * values. Loaders do not run themselves and write nothing: {@link CardRegistry} calls this once per
 * set and performs every registration from the result, so a property naming no provider leaves the
 * registry unconstructible and the context fails to start.
 *
 * <p>An implementation is a pure function of its set code plus its cache directory. Everything
 * provider-specific — field naming, face resolution, HTTP, caching — stops here;
 * {@code OracleLoaderIntegrationTest} is what holds the two implementations to producing identical
 * registries.
 */
public interface OracleLoader {

    /**
     * @param setCode                     the set to read, e.g. {@code "ISD"}
     * @param implementedCollectorNumbers which printings the game actually implements. Only these
     *                                    need their oracle text parsed — a set is mostly cards with
     *                                    no class behind them, and parsing all of them would be
     *                                    work thrown away. Set-wide fields
     *                                    ({@link SetOracleData#cardTotal},
     *                                    {@link SetOracleData#rarityByCollectorNumber}) still cover
     *                                    the whole set.
     * @throws RuntimeException if the source is unreachable or its data cannot be parsed. Startup
     *                          fails rather than falling back to the other source: a silent swap
     *                          would let one build serve different oracle text than the next.
     */
    SetOracleData loadSet(String setCode, Set<String> implementedCollectorNumbers);
}
