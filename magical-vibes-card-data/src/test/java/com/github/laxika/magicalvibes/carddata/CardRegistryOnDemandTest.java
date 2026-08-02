package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.cards.CardScanner;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.r.RavagerOfTheFells;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.OracleData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardRegistryOnDemandTest {

    private CardRegistry registry;

    @BeforeEach
    void clearOracleData() {
        Card.clearOracleRegistry();
    }

    @AfterEach
    void closeRegistry() {
        if (registry != null) {
            registry.close();
        }
        Card.clearOracleRegistry();
    }

    @Test
    void loadsOnlySetsRequestedByCardConstructionOrCatalogLookup() {
        RecordingLoader loader = new RecordingLoader();
        registry = new CardRegistry(loader, OracleLoadMode.ON_DEMAND);
        registry.load();

        assertThat(loader.loadedSetCodes).isEmpty();

        Pacifism first = new Pacifism();
        Pacifism second = new Pacifism();

        // Which of Pacifism's printings the registry prefers is not this test's subject, and it
        // moves every time the card gains a reprint in a newly added set. Both constructions must
        // resolve through one set, loaded once.
        assertThat(loader.loadedSetCodes).hasSize(1);
        String preferredSet = loader.loadedSetCodes.getFirst();
        String collectorNumber = CardScanner.collectorNumberOf(Pacifism.class, preferredSet).orElseThrow();
        assertThat(first.getName()).isEqualTo(preferredSet + " #" + collectorNumber);
        assertThat(second.getName()).isEqualTo(preferredSet + " #" + collectorNumber);

        // The catalog lookup below only proves anything if it names a set construction did not load.
        assertThat(preferredSet).isNotEqualTo(CardSet.SET_10E.getCode());

        registry.findByCollectorNumber(CardSet.SET_10E, "31");
        registry.findByCollectorNumber(CardSet.SET_10E, "31");

        assertThat(loader.loadedSetCodes).containsExactly(preferredSet, "10E");
    }

    @Test
    void failedLoadsRemainRetryableAndBackFaceOnlyClassesResolveThroughTheirFrontPrinting() {
        RecordingLoader loader = new RecordingLoader();
        loader.failNextLoadOf("DKA");
        registry = new CardRegistry(loader, OracleLoadMode.ON_DEMAND);
        registry.load();

        assertThatThrownBy(RavagerOfTheFells::new)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DKA");

        RavagerOfTheFells ravager = new RavagerOfTheFells();

        assertThat(ravager.getName()).isEqualTo("Ravager of the Fells");
        assertThat(loader.loadedSetCodes).containsExactly("DKA", "DKA");
    }

    private static final class RecordingLoader implements OracleLoader {

        private final List<String> loadedSetCodes = new ArrayList<>();
        private final Set<String> setsFailingNextLoad = new HashSet<>();

        void failNextLoadOf(String setCode) {
            setsFailingNextLoad.add(setCode);
        }

        @Override
        public SetOracleData loadSet(String setCode, Set<String> implementedCollectorNumbers) {
            loadedSetCodes.add(setCode);
            if (setsFailingNextLoad.remove(setCode)) {
                throw new RuntimeException("Failed to load " + setCode);
            }

            Map<String, OracleData> fronts = new HashMap<>();
            for (String collectorNumber : implementedCollectorNumbers) {
                fronts.put(collectorNumber, oracle(setCode + " #" + collectorNumber));
            }

            Map<String, OracleData> backs = new HashMap<>();
            if (setCode.equals("DKA") && implementedCollectorNumbers.contains("140")) {
                backs.put("140", oracle("Ravager of the Fells"));
            }
            return new SetOracleData(setCode, implementedCollectorNumbers.size(),
                    Map.of(), fronts, backs, Map.of());
        }

        private static OracleData oracle(String name) {
            return new OracleData(name, CardType.ENCHANTMENT, Set.of(), "{1}{W}", null, List.of(),
                    List.of(), Set.of(), List.of(), null, null, null, Set.of(), null, null, null);
        }
    }
}
