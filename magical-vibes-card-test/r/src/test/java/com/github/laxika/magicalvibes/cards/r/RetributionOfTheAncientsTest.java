package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.v.ValleyDasher;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RetributionOfTheAncients.class, ValleyDasher.class})
class RetributionOfTheAncientsTest extends BaseCardTest {

    @Test
    @DisplayName("Removes counters from your creatures and gives a creature -X/-X")
    void removesCountersAndShrinksTarget() {
        addEnchantment();
        Permanent source = addCreatureReady(player1, new ValleyDasher());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent target = addCreatureReady(player2, new ValleyDasher());
        addBlackMana();

        harness.activateAbility(player1, 0, 0, 2, target.getId());
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, target)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, target)).isZero();
    }

    @Test
    @DisplayName("The -X/-X effect wears off at end of turn")
    void shrinkWearsOffAtEndOfTurn() {
        addEnchantment();
        Permanent source = addCreatureReady(player1, new ValleyDasher());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new ValleyDasher());
        addBlackMana();

        harness.activateAbility(player1, 0, 0, 1, target.getId());
        harness.passBothPriorities();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.passUntil(com.github.laxika.magicalvibes.model.TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("X can be zero without any counters or controlled creatures")
    void allowsZeroCounters() {
        addEnchantment();
        Permanent target = addCreatureReady(player2, new ValleyDasher());
        addBlackMana();

        harness.activateAbility(player1, 0, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Valley Dasher");
    }

    @Test
    @DisplayName("Counter payment can be split across creatures and kills a zero-toughness target")
    void splitsCounterPaymentAcrossCreatures() {
        addEnchantment();
        Permanent first = addCreatureReady(player1, new ValleyDasher());
        Permanent second = addCreatureReady(player1, new ValleyDasher());
        first.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new ValleyDasher());
        addBlackMana();

        harness.activateAbility(player1, 0, 0, 2, target.getId());

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Valley Dasher");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Valley Dasher");
        harness.assertInGraveyard(player2, "Valley Dasher");
    }

    @Test
    @DisplayName("A creature can both pay the counter cost and be the target")
    void targetsCreaturePayingCost() {
        addEnchantment();
        Permanent target = addCreatureReady(player1, new ValleyDasher());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addBlackMana();

        harness.activateAbility(player1, 0, 0, 1, target.getId());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counters on an opponent's creatures cannot pay the cost")
    void ignoresOpponentCounters() {
        addEnchantment();
        addCreatureReady(player1, new ValleyDasher());
        Permanent opponentCreature = addCreatureReady(player2, new ValleyDasher());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addBlackMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void addEnchantment() {
        harness.addToBattlefield(player1, new RetributionOfTheAncients());
    }

    private void addBlackMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
