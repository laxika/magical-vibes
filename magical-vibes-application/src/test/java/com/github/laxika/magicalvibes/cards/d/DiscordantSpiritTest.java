package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(DiscordantSpirit.class)
class DiscordantSpiritTest extends BaseCardTest {

    private Permanent addSpirit() {
        return addCreatureReady(player1, new DiscordantSpirit());
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    private void advanceToEndStepAndResolve(Player activePlayer) {
        advanceToEndStep(activePlayer);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Gets a +1/+1 counter per damage dealt to its controller during the opponent's end step")
    void gainsCountersOnOpponentTurn() {
        Permanent spirit = addSpirit();
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        advanceToEndStepAndResolve(player2);

        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @CardUsed(Incinerate.class)
    @DisplayName("Counts damage dealt after the opponent's end-step trigger is put on the stack")
    void countsDamageDealtAfterTriggerIsPutOnStack() {
        Permanent spirit = addSpirit();

        advanceToEndStep(player2);
        harness.passPriority(player2);
        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets no counters on the opponent's end step when no damage was dealt to its controller")
    void noCountersWithoutDamage() {
        Permanent spirit = addSpirit();

        advanceToEndStepAndResolve(player2);

        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Damage dealt to the opponent does not grow it")
    void ignoresDamageToOpponent() {
        Permanent spirit = addSpirit();
        gd.recordDamageToPlayer(player2.getId(), 4);

        advanceToEndStepAndResolve(player2);

        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("No counters are added during its controller's own end step")
    void doesNotGrowOnControllerTurn() {
        Permanent spirit = addSpirit();
        gd.recordDamageToPlayer(player1.getId(), 2);

        advanceToEndStepAndResolve(player1);

        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("All +1/+1 counters are removed during its controller's end step")
    void removesCountersOnControllerEndStep() {
        Permanent spirit = addSpirit();
        spirit.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);

        advanceToEndStepAndResolve(player1);

        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Counters gained on the opponent's turn survive that turn's end step")
    void countersSurviveOpponentEndStep() {
        Permanent spirit = addSpirit();
        spirit.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        gd.recordDamageToPlayer(player1.getId(), 1);

        advanceToEndStepAndResolve(player2);

        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }
}
