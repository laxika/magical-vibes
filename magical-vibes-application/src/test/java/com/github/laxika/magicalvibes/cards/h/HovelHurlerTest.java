package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HovelHurler.class, GrizzlyBears.class})
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

    @Test
    @DisplayName("Can pay with white and remove a +1/+1 counter while tapped and summoning sick")
    void acceptsWhiteManaAndOtherCounterTypes() {
        Permanent hurler = harness.addToBattlefieldAndReturn(player1, new HovelHurler());
        hurler.tap();
        hurler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HovelHurler());
        int originalPower = gqs.getEffectivePower(gd, target);
        int originalToughness = gqs.getEffectiveToughness(gd, target);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(hurler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(originalToughness);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(hurler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can remove a non-power counter and pay with mixed hybrid colors")
    void acceptsStunCounterAndMixedMana() {
        Permanent hurler = harness.addToBattlefieldAndReturn(player1, new HovelHurler());
        hurler.setCounterCount(CounterType.STUN, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HovelHurler());
        int originalPower = gqs.getEffectivePower(gd, target);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(hurler.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower + 1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without a counter")
    void cannotActivateWithoutCounters() {
        harness.addToBattlefield(player1, new HovelHurler());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HovelHurler());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent hurler = harness.addToBattlefieldAndReturn(player1, new HovelHurler());
        hurler.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HovelHurler());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(hurler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate during upkeep on its controller's turn")
    void cannotActivateOutsideMainPhase() {
        Permanent hurler = harness.addToBattlefieldAndReturn(player1, new HovelHurler());
        hurler.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HovelHurler());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(hurler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate again while the first activation is on the stack")
    void cannotActivateWithNonemptyStack() {
        Permanent hurler = harness.addToBattlefieldAndReturn(player1, new HovelHurler());
        hurler.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HovelHurler());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 4);
        harness.activateAbility(player1, 0, null, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(hurler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Controller chooses which counter type to spend when several are available")
    void asksWhichCounterToRemove() {
        Permanent hurler = harness.addToBattlefieldAndReturn(player1, new HovelHurler());
        hurler.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        hurler.setCounterCount(CounterType.FINALITY, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HovelHurler());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(hurler.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(hurler.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
    }
}
