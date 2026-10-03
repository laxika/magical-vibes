package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.carddata.mtgjson.MtgjsonOracleLoader;
import com.github.laxika.magicalvibes.cards.CardPrinting;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.cards.e.EsperSentinel;
import com.github.laxika.magicalvibes.cards.k.KarnTheGreatCreator;
import com.github.laxika.magicalvibes.cards.s.SenseisDiviningTop;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.cards.t.ThoughtKnotSeer;
import com.github.laxika.magicalvibes.cards.w.WeatheredWayfarer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("scryfall")
class SldPrintingOracleTest {

    private MtgjsonOracleLoader loader;
    private CardRegistry registry;

    @BeforeEach
    void setUp() {
        Card.clearOracleRegistry();
        loader = new MtgjsonOracleLoader(
                System.getProperty("card-data.cache-dir", "./card-data-cache"));
        registry = new CardRegistry(loader, OracleLoadMode.ON_DEMAND);
        registry.load();
    }

    @AfterEach
    void tearDown() {
        registry.close();
        Card.clearOracleRegistry();
    }

    @Test
    void constructingKarnLoadsSldWithoutMisfiledOracleData() {
        // Random deck pool construction first exposed the bad Thought-Knot Seer registration
        // while resolving Karn's oracle data: loading a set validates all its printings.
        assertThat(new KarnTheGreatCreator().getName()).isEqualTo("Karn, the Great Creator");
        registry.ensureSetLoaded(CardSet.SET_SLD);

        Card seer = new ThoughtKnotSeer();
        assertThat(seer.getName()).isEqualTo("Thought-Knot Seer");
        assertThat(seer.getManaCost()).isEqualTo("{3}{C}");
        assertThat(seer.getType()).isEqualTo(CardType.CREATURE);
        assertThat(seer.getAdditionalTypes()).doesNotContain(CardType.ARTIFACT);
        assertThat(seer.getPower()).isEqualTo(4);
        assertThat(seer.getToughness()).isEqualTo(4);
    }

    @Test
    void everySldSeerPrintingHasItsOwnMatchingOracleData() {
        List<CardPrinting> printings = registry.getPrintings(CardSet.SET_SLD).stream()
                .filter(printing -> printing.cardClassName().equals(ThoughtKnotSeer.class.getName()))
                .toList();
        assertThat(printings).isNotEmpty();
        Set<String> numbers = printings.stream()
                .map(CardPrinting::collectorNumber)
                .collect(Collectors.toSet());
        SetOracleData data = loader.loadSet("SLD", numbers);

        for (CardPrinting printing : printings) {
            // Read each printing directly so another reprint cannot hide a wrong registration.
            assertThat(data.frontFaceByCollectorNumber().get(printing.collectorNumber()))
                    .as("SLD #%s", printing.collectorNumber())
                    .isNotNull()
                    .satisfies(oracle -> assertThat(oracle.name()).isEqualTo("Thought-Knot Seer"));
        }
    }

    @ParameterizedTest
    @MethodSource("correctedPrintings")
    void correctedPrintingConstructsTheMatchingCard(
            String collectorNumber, Class<? extends Card> expectedClass, String expectedName) {
        Card card = registry.findByCollectorNumber(CardSet.SET_SLD, collectorNumber).createCard();

        assertThat(card).isInstanceOf(expectedClass);
        assertThat(card.getName()).isEqualTo(expectedName);
        assertThat(card.getSetCode()).isEqualTo("SLD");
        assertThat(card.getCollectorNumber()).isEqualTo(collectorNumber);
    }

    private static Stream<Arguments> correctedPrintings() {
        return Stream.of(
                Arguments.of("2123", EsperSentinel.class, "Esper Sentinel"),
                Arguments.of("2125", SwordsToPlowshares.class, "Swords to Plowshares"),
                Arguments.of("2126", WeatheredWayfarer.class, "Weathered Wayfarer"),
                Arguments.of("2131", SenseisDiviningTop.class, "Sensei's Divining Top"),
                Arguments.of("2132", SolRing.class, "Sol Ring")
        );
    }
}
