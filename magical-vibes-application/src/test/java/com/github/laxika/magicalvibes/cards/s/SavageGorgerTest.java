package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.s.Shock;
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

class SavageGorgerTest extends BaseCardTest {

    @Test
    @DisplayName("End-step ability puts a counter on Savage Gorger when an opponent lost life")
    void endStepCounterTriggersForOpponentLifeLoss() {
        Permanent gorger = addReadyGorger();
        dealDamage(player2);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gorger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("End-step ability does not trigger when only its controller lost life")
    void endStepCounterDoesNotTriggerForControllerLifeLoss() {
        Permanent gorger = addReadyGorger();
        dealDamage(player1);

        advanceToEndStep(player1);

        assertThat(gorger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("End-step ability does not trigger when no opponent lost life")
    void endStepCounterDoesNotTriggerWithoutOpponentLifeLoss() {
        Permanent gorger = addReadyGorger();

        advanceToEndStep(player1);

        assertThat(gorger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyGorger() {
        Permanent gorger = harness.addToBattlefieldAndReturn(player1, new SavageGorger());
        gorger.setSummoningSick(false);
        return gorger;
    }

    private void dealDamage(Player target) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
