package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.carddata.mtgjson.MtgjsonSetSource;
import com.github.laxika.magicalvibes.carddata.scryfall.ScryfallSetSource;
import com.github.laxika.magicalvibes.carddata.store.SetDataStore;
import com.github.laxika.magicalvibes.carddata.store.StoredSetOracleLoader;

import java.util.List;

/**
 * The production loader over the shared test cache selected by {@code card-data.cache-dir}, for
 * tests that check invariants against the real stored set data.
 */
final class SharedCacheLoaders {

    static final String CACHE_DIR = System.getProperty("card-data.cache-dir", "./card-data-cache");

    private SharedCacheLoaders() {
    }

    /** Prefers MTGJSON for a set missing from the cache, as the Gradle test tasks do. */
    static StoredSetOracleLoader storedSets() {
        return new StoredSetOracleLoader(new SetDataStore(CACHE_DIR, OracleDataProvider.MTGJSON.name(),
                List.of(new ScryfallSetSource(), new MtgjsonSetSource())));
    }
}
