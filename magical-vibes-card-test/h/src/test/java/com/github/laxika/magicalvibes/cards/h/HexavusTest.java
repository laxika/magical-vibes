package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Hexavus.class, GrizzlyBears.class})
class HexavusTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with six +1/+1 counters")
    void entersWithSixPlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new Hexavus()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent hexavus = findPermanent(player1, "Hexavus");
        assertThat(hexavus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(hexavus.getEffectivePower()).isEqualTo(6);
        assertThat(hexavus.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Removes a +1/+1 counter to put a flying counter on another creature")
    void putsFlyingCounterOnAnotherCreature() {
        Permanent hexavus = addReadyHexavus(6);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(hexavus), 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(hexavus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(bears.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The flying-counter ability cannot target Hexavus itself")
    void flyingCounterAbilityCannotTargetItself() {
        Permanent hexavus = addReadyHexavus(6);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(hexavus), 0, null, hexavus.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature");
    }

    @Test
    @DisplayName("Removes another creature's counter to put a +1/+1 counter on Hexavus")
    void removesAnotherCreatureCounterToGrow() {
        Permanent hexavus = addReadyHexavus(6);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.FLYING, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(hexavus), 1, null, null);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(hexavus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
    }

    @Test
    @DisplayName("The growth ability cannot remove a counter from Hexavus itself")
    void growthAbilityRequiresAnotherCreature() {
        Permanent hexavus = addReadyHexavus(6);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(hexavus), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent you control has a counter");
    }

    private Permanent addReadyHexavus(int counters) {
        Permanent hexavus = addCreatureReady(player1, new Hexavus());
        hexavus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return hexavus;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
