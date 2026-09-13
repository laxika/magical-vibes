package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.carddata.mtgjson.MtgjsonOracleLoader;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardScanner;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.OracleData;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("scryfall")
class ArenaPrintingOracleTest {

    @Test
    void everyArenaPrintingHasItsOwnMatchingOracleData() {
        MtgjsonOracleLoader loader = new MtgjsonOracleLoader(
                System.getProperty("card-data.cache-dir", "./card-data-cache"));
        Map<CardSet, List<CardPrinting>> printings = CardScanner.scan();

        for (CardSet set : List.of(CardSet.SET_ANA, CardSet.SET_OANA, CardSet.SET_XANA)) {
            Set<String> numbers = printings.get(set).stream()
                    .map(CardPrinting::collectorNumber)
                    .collect(Collectors.toSet());
            SetOracleData data = loader.loadSet(set.getCode(), numbers);
            for (CardPrinting printing : printings.get(set)) {
                // Checking the set data directly prevents another reprint's oracle registration
                // from hiding a nonexistent collector number in one of the Arena sets.
                OracleData front = data.frontFaceByCollectorNumber().get(printing.collectorNumber());
                assertThat(front)
                        .as("oracle data for %s #%s (%s)", set.getCode(), printing.collectorNumber(),
                                printing.simpleCardClassName())
                        .isNotNull();
                assertThat(CardRegistry.matchesClassName(printing.simpleCardClassName(), front.name(), null))
                        .as("%s #%s must describe %s, got %s", set.getCode(), printing.collectorNumber(),
                                printing.simpleCardClassName(), front.name())
                        .isTrue();
            }
        }
    }
}
