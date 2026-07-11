package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.carddata.mtgjson.MtgjsonOracleLoader;
import com.github.laxika.magicalvibes.carddata.scryfall.ScryfallOracleLoader;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.cards.r.RavagerOfTheFells;
import com.github.laxika.magicalvibes.cards.s.SeethingSong;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs each oracle loader end to end against the real cached set data (first run fetches from
 * the network and populates this module's {@code ./card-data-cache}, like the other test modules)
 * and verifies the registration invariants that parsing-level unit tests cannot: every printing
 * resolves to oracle data, back-face-only classes get registered, and back-face registrations
 * do not clobber a standalone card's own printing (the Seething Song collision).
 */
@Tag("scryfall")
class OracleLoaderIntegrationTest {

    private static final String CACHE_DIR = "./card-data-cache";

    /** Excluded on CI ("scryfall-api" tag): CI loads oracle data exclusively from MTGJSON. */
    @Test
    @Tag("scryfall-api")
    void scryfallLoadRegistersEveryPrintingAndSurvivesBackFaceCollision() {
        Card.clearOracleRegistry();
        ScryfallOracleLoader.loadAll(CACHE_DIR);

        assertLoadedRegistryInvariants();
    }

    @Test
    void mtgjsonLoadRegistersEveryPrintingAndSurvivesBackFaceCollision() {
        Card.clearOracleRegistry();
        MtgjsonOracleLoader.loadAll(CACHE_DIR);

        assertLoadedRegistryInvariants();
    }

    private void assertLoadedRegistryInvariants() {
        // SOS 109 (Blazing Firesinger // Seething Song) reuses the SeethingSong class as its
        // back face; the 9ED printing's full oracle data must win over the face-node data,
        // which lacks colors in Scryfall's case
        assertThat(new SeethingSong().getColor()).isEqualTo(CardColor.RED);
        assertThat(new SeethingSong().getColors()).containsExactly(CardColor.RED);

        // Back-face-only classes (transform back faces without their own printing) must still
        // be registered
        assertThat(new RavagerOfTheFells().getName()).isEqualTo("Ravager of the Fells");

        // Every implemented printing must resolve to oracle data
        for (CardSet set : CardSet.values()) {
            for (CardPrinting printing : set.getPrintings()) {
                Card card = printing.factory().get();
                assertThat(card.getName())
                        .as("oracle data for %s #%s (%s)", set.getCode(), printing.collectorNumber(),
                                card.getClass().getSimpleName())
                        .isNotNull();
            }
        }
    }
}
