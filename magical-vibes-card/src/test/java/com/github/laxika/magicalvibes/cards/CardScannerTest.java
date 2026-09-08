package com.github.laxika.magicalvibes.cards;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CardScannerTest {

    private static Map<CardSet, List<CardPrinting>> printings;

    @BeforeAll
    static void scanRegisteredCards() {
        printings = CardScanner.scan();
    }

    @ParameterizedTest
    @ValueSource(strings = {"CP1", "CP2", "CP3"})
    void indexesReprintRegistrations(String setCode) {
        CardSet set = CardSet.findByCode(setCode);

        assertThat(set).isNotNull();
        assertThat(printings.get(set))
                .extracting(CardPrinting::collectorNumber)
                .contains("1", "2", "3", "4", "5", "6");
    }
}
