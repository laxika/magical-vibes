package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HovelHurlerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two -1/-1 counters")
    void entersWithMinusOneMinusOneCounters() {
        harness.setHand(player1, List.of(new HovelHurler()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent hurler = findPermanent(player1, "Hovel Hurler");
        assertThat(hurler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes a -1/-1 counter to boost another creature and grant flying")
    void removesCounterToBoostAnotherCreature() {
        Permanent hurler = addCreatureReady(player1, new HovelHurler());
        hurler.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(hurler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The boost and flying wear off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent hurler = addCreatureReady(player1, new HovelHurler());
        hurler.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target Hovel Hurler itself")
    void cannotTargetItself() {
        Permanent hurler = addCreatureReady(player1, new HovelHurler());
        hurler.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, hurler.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(hurler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void sorcerySpeedOnly() {
        Permanent hurler = addCreatureReady(player1, new HovelHurler());
        hurler.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
