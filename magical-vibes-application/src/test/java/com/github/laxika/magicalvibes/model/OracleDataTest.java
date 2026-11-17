package com.github.laxika.magicalvibes.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class OracleDataTest {

    @Test
    void manaValueCountsTheManaCostWithXAsZero() {
        assertThat(oracle(CardType.SORCERY, Set.of(), "{X}{R}", null).manaValue()).isEqualTo(1);
        assertThat(oracle(CardType.CREATURE, Set.of(), "{3}{G}{G}", null).manaValue()).isEqualTo(5);
    }

    @Test
    void manaValueIsZeroWithoutAManaCost() {
        assertThat(oracle(CardType.LAND, Set.of(), null, null).manaValue()).isZero();
    }

    @Test
    void splitCardManaValueIsTheCombinedValueOfItsHalves() {
        assertThat(oracle(CardType.SORCERY, Set.of(), "{3}{W}{W}", 10).manaValue()).isEqualTo(10);
    }

    @Test
    void hasTypeReadsTheMainAndAdditionalTypes() {
        OracleData artifactCreature = oracle(CardType.ARTIFACT, Set.of(CardType.CREATURE), "{2}", null);

        assertThat(artifactCreature.hasType(CardType.ARTIFACT)).isTrue();
        assertThat(artifactCreature.hasType(CardType.CREATURE)).isTrue();
        assertThat(artifactCreature.hasType(CardType.ENCHANTMENT)).isFalse();
    }

    @Test
    void namesAreEachSplitHalfOrTheOneName() {
        assertThat(named("Commit // Memory").names()).containsExactly("Commit", "Memory");
        assertThat(named("Grizzly Bears").names()).containsExactly("Grizzly Bears");
        assertThat(named(null).names()).isEmpty();
    }

    @Test
    void cardBuiltFromTheDataAgreesWithIt() {
        OracleData data = oracle(CardType.ARTIFACT, Set.of(CardType.CREATURE), "{X}{2}{U}", null);
        // Registered under a class name no real card has, and left registered: clearing the
        // registry would strip the oracle data of every set other tests in this JVM loaded.
        Card.registerOracle(OracleDataProbeCard.class.getSimpleName(), data);

        Card card = new OracleDataProbeCard();

        assertThat(card.getManaValue()).isEqualTo(data.manaValue());
        assertThat(card.getNames()).isEqualTo(data.names());
        for (CardType cardType : CardType.values()) {
            assertThat(card.hasType(cardType)).as(cardType.name()).isEqualTo(data.hasType(cardType));
        }
    }

    private static OracleData named(String name) {
        return new OracleData(name, CardType.INSTANT, Set.of(), "{1}", null, List.of(), List.of(),
                Set.of(), List.of(), null, null, null, Set.of(), null, null, null, null);
    }

    private static OracleData oracle(CardType type, Set<CardType> additionalTypes, String manaCost,
                                     Integer combinedManaValue) {
        return new OracleData("Probe", type, additionalTypes, manaCost, null, List.of(), List.of(),
                Set.of(), List.of(), null, null, null, Set.of(), null, null, null, combinedManaValue);
    }

    private static final class OracleDataProbeCard extends Card {
    }
}
