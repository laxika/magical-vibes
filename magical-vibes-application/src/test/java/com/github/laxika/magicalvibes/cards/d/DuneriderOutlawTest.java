package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DuneriderOutlaw.class, GrizzlyBears.class})
class DuneriderOutlawTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter at the end step after dealing damage to an opponent")
    void getsCounterAtEndStepAfterDealingDamage() {
        Permanent outlaw = addCreatureReady(player1, new DuneriderOutlaw());
        outlaw.setAttacking(true);

        resolveCombat();

        assertThat(outlaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(outlaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a counter at the end step without dealing damage to an opponent")
    void noCounterWithoutDamageToOpponent() {
        Permanent outlaw = addCreatureReady(player1, new DuneriderOutlaw());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(outlaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Cannot be blocked by a green creature")
    void greenCreatureCannotBlock() {
        Permanent outlaw = addCreatureReady(player1, new DuneriderOutlaw());
        outlaw.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }
}
