package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HeartmenderTest extends BaseCardTest {

    private void advanceToUpkeepAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP, firing upkeep triggers
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 50) {
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Upkeep removes one -1/-1 counter from each creature you control, clamping at zero")
    void upkeepRemovesOneMinusCounterFromEachControlledCreature() {
        harness.addToBattlefield(player1, new Heartmender());
        Permanent bearsA = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bearsB = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bearsA.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);
        bearsB.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        advanceToUpkeepAndResolve(player1);

        assertThat(bearsA.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(bearsB.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Upkeep also removes a counter from Heartmender itself and leaves counterless creatures alone")
    void upkeepAffectsSelfAndSkipsCounterlessCreatures() {
        Permanent heartmender = harness.addToBattlefieldAndReturn(player1, new Heartmender());
        Permanent counterless = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        heartmender.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        advanceToUpkeepAndResolve(player1);

        assertThat(heartmender.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
        assertThat(counterless.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Upkeep does not remove counters from creatures an opponent controls")
    void upkeepDoesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player1, new Heartmender());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentBears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        advanceToUpkeepAndResolve(player1);

        assertThat(opponentBears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }
}
