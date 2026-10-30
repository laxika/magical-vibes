package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.carddata.scryfall.ScryfallSetSource;
import com.github.laxika.magicalvibes.carddata.store.SetDataStore;
import com.github.laxika.magicalvibes.carddata.store.StoredSetOracleLoader;
import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.cards.r.RavagerOfTheFells;
import com.github.laxika.magicalvibes.cards.s.SeethingSong;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Loads every set end to end and verifies the registration invariants that conversion-level unit
 * tests cannot: every printing resolves to oracle data, back-face-only classes get registered, and
 * back-face registrations do not clobber a standalone card's own printing (the Seething Song
 * collision).
 *
 * <p>The default run reads the shared test cache selected by {@code card-data.cache-dir}, preferring
 * MTGJSON for missing sets as the test tasks do. The Scryfall run stores into a subdirectory of its
 * own with no fallback: in the shared directory every set would already be stored, whichever
 * provider it came from, and the run would test nothing about Scryfall. The first run of each
 * downloads; later runs read the stored sets.
 */
@Tag("scryfall")
class OracleLoaderIntegrationTest {

    /** Excluded on CI ("scryfall-api" tag): CI must not depend on the Scryfall API. */
    @Test
    @Disabled("Loads every set from the Scryfall API: run by hand when needed, to spare Scryfall")
    @Tag("scryfall-api")
    void scryfallLoadRegistersEveryPrintingAndSurvivesBackFaceCollision() {
        Card.clearOracleRegistry();

        String cacheDir = Path.of(SharedCacheLoaders.CACHE_DIR).resolve("only-scryfall").toString();
        assertLoadedRegistryInvariants(loadWith(new StoredSetOracleLoader(new SetDataStore(
                cacheDir, OracleDataProvider.SCRYFALL.name(), List.of(new ScryfallSetSource())))));
    }

    @Test
    void sharedCacheLoadRegistersEveryPrintingAndSurvivesBackFaceCollision() {
        Card.clearOracleRegistry();

        assertLoadedRegistryInvariants(loadWith(SharedCacheLoaders.storedSets()));
    }

    /** Drives the loader through a registry of its own, as the {@code @PostConstruct} does. */
    private static CardRegistry loadWith(OracleLoader loader) {
        CardRegistry registry = new CardRegistry(loader);
        registry.load();
        return registry;
    }

    private void assertLoadedRegistryInvariants(CardCatalog catalog) {
        // SOS 109 (Blazing Firesinger // Seething Song) reuses the SeethingSong class as its
        // back face; the 9ED printing's full oracle data must win over the face-node data,
        // which lacks colors in Scryfall's case
        assertThat(new SeethingSong().getColor()).isEqualTo(CardColor.RED);
        assertThat(new SeethingSong().getColors()).containsExactly(CardColor.RED);

        // Back-face-only classes (transform back faces without their own printing) must still
        // be registered
        assertThat(new RavagerOfTheFells().getName()).isEqualTo("Ravager of the Fells");

        // Every implemented printing must resolve to oracle data
        List<String> missingOracleData = new ArrayList<>();
        for (CardSet set : CardSet.values()) {
            for (CardPrinting printing : catalog.getPrintings(set)) {
                Card card = printing.factory().get();
                if (card.getName() == null) {
                    missingOracleData.add("%s #%s (%s)".formatted(set.getCode(), printing.collectorNumber(),
                            card.getClass().getSimpleName()));
                }
            }
        }
        assertThat(missingOracleData).as("printings without oracle data").isEmpty();
    }
}
