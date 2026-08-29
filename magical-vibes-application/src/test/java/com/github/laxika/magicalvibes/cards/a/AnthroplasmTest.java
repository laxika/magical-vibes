package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnthroplasmTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters")
    void entersWithTwoCounters() {
        prepareMainPhase();
        harness.setHand(player1, List.of(new Anthroplasm()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findAnthroplasm(player1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Paid X replaces all +1/+1 counters with X counters")
    void replacesCountersWithPaidX() {
        Permanent anthroplasm = addReadyAnthroplasm(2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        prepareMainPhase();

        harness.activateAbility(player1, 0, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(anthroplasm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Activation taps Anthroplasm")
    void activationTapsAnthroplasm() {
        Permanent anthroplasm = addReadyAnthroplasm(2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase();

        harness.activateAbility(player1, 0, 0, 1, null, null);

        assertThat(anthroplasm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent anthroplasm = addReadyAnthroplasm(2);
        anthroplasm.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyAnthroplasm(int counters) {
        Permanent anthroplasm = harness.addToBattlefieldAndReturn(player1, new Anthroplasm());
        anthroplasm.setSummoningSick(false);
        anthroplasm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return anthroplasm;
    }

    private Permanent findAnthroplasm(Player player) {
        return findPermanent(player, "Anthroplasm");
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
