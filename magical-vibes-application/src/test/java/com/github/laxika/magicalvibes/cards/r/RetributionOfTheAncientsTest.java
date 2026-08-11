package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetributionOfTheAncientsTest extends BaseCardTest {

    @Test
    @DisplayName("Removes counters from your creatures and gives a creature -X/-X")
    void removesCountersAndShrinksTarget() {
        addEnchantment();
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
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
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addBlackMana();

        harness.activateAbility(player1, 0, 0, 1, target.getId());
        harness.passBothPriorities();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Requires at least one +1/+1 counter to be removed")
    void rejectsZeroCounters() {
        addEnchantment();
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addBlackMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters on an opponent's creatures cannot pay the cost")
    void ignoresOpponentCounters() {
        addEnchantment();
        addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
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
