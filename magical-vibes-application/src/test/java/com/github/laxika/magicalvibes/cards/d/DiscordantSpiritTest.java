package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DiscordantSpiritTest extends BaseCardTest {

    private Permanent addSpirit() {
        Permanent spirit = new Permanent(new DiscordantSpirit());
        gd.playerBattlefields.get(player1.getId()).add(spirit);
        return spirit;
    }

    private void advanceToEndStepAndResolve(UUID activePlayerId) {
        harness.forceActivePlayer(activePlayerId.equals(player1.getId()) ? player1 : player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gets a +1/+1 counter per damage dealt to its controller during the opponent's end step")
    void gainsCountersOnOpponentTurn() {
        Permanent spirit = addSpirit();
        gd.recordDamageToPlayer(player1.getId(), 3);

        advanceToEndStepAndResolve(player2.getId());

        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets no counters on the opponent's end step when no damage was dealt to its controller")
    void noCountersWithoutDamage() {
        Permanent spirit = addSpirit();

        advanceToEndStepAndResolve(player2.getId());

        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Damage dealt to the opponent does not grow it")
    void ignoresDamageToOpponent() {
        Permanent spirit = addSpirit();
        gd.recordDamageToPlayer(player2.getId(), 4);

        advanceToEndStepAndResolve(player2.getId());

        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("No counters are added during its controller's own end step")
    void doesNotGrowOnControllerTurn() {
        Permanent spirit = addSpirit();
        gd.recordDamageToPlayer(player1.getId(), 2);

        advanceToEndStepAndResolve(player1.getId());

        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("All +1/+1 counters are removed during its controller's end step")
    void removesCountersOnControllerEndStep() {
        Permanent spirit = addSpirit();
        spirit.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 5);

        advanceToEndStepAndResolve(player1.getId());

        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Counters gained on the opponent's turn survive that turn's end step")
    void countersSurviveOpponentEndStep() {
        Permanent spirit = addSpirit();
        spirit.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        gd.recordDamageToPlayer(player1.getId(), 1);

        advanceToEndStepAndResolve(player2.getId());

        assertThat(spirit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }
}
