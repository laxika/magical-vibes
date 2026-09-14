package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.cards.RandomDeckGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RandomDeckGeneratorInitializationTest {

    @Test
    void eagerStartupBuildsThePoolBeforeTheFirstRequest() {
        RecordingRegistry registry = new RecordingRegistry();

        RandomDeckGenerator generator = new CardDataConfiguration()
                .randomDeckGenerator(registry, OracleLoadMode.EAGER);

        assertThat(registry.requestedSets).containsExactly(CardSet.values());
        assertThat(registry.landLookups).isEqualTo(5);

        generator.hasDeckableCards(null);
        generator.initializeCardPool();

        assertThat(registry.requestedSets).containsExactly(CardSet.values());
        assertThat(registry.landLookups).isEqualTo(5);
    }

    @ParameterizedTest
    @EnumSource(value = OracleLoadMode.class, names = {"ON_DEMAND", "EXPLICIT"})
    void testLoadingModesBuildThePoolOnlyWhenRequested(OracleLoadMode loadMode) {
        RecordingRegistry registry = new RecordingRegistry();

        RandomDeckGenerator generator = new CardDataConfiguration()
                .randomDeckGenerator(registry, loadMode);

        assertThat(registry.requestedSets).isEmpty();
        assertThat(registry.landLookups).isZero();

        generator.hasDeckableCards(null);
        generator.hasDeckableCards(null);

        assertThat(registry.requestedSets).containsExactly(CardSet.values());
        assertThat(registry.landLookups).isEqualTo(5);
    }

    private static final class RecordingRegistry extends CardRegistry {

        private final List<CardSet> requestedSets = new ArrayList<>();
        private int landLookups;

        private RecordingRegistry() {
            super((setCode, implemented) -> {
                throw new AssertionError("This test should only read the catalog");
            });
        }

        @Override
        public List<CardPrinting> getPrintings(CardSet set) {
            requestedSets.add(set);
            return List.of();
        }

        @Override
        public CardPrinting findByCollectorNumber(CardSet set, String collectorNumber) {
            landLookups++;
            return null;
        }
    }
}
