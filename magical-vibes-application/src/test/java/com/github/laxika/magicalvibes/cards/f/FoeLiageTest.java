package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CommandTower;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FoeLiage.class, CommandTower.class})
class FoeLiageTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when a land enters during its controller's turn")
    void getsCounterWhenLandEntersDuringItsControllersTurn() {
        Permanent foeLiage = harness.addToBattlefieldAndReturn(player1, new FoeLiage());

        harness.enterBattlefieldAndReturn(player1, new CommandTower());
        harness.passBothPriorities();

        assertThat(foeLiage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers for a land entering under any player's control during its controller's turn")
    void getsCounterForOpponentsLandDuringItsControllersTurn() {
        Permanent foeLiage = harness.addToBattlefieldAndReturn(player1, new FoeLiage());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.enterBattlefieldAndReturn(player2, new CommandTower());
        harness.passBothPriorities();

        assertThat(foeLiage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when a land enters during an opponent's turn")
    void doesNotTriggerDuringOpponentsTurn() {
        Permanent foeLiage = harness.addToBattlefieldAndReturn(player1, new FoeLiage());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.enterBattlefieldAndReturn(player1, new CommandTower());
        harness.passBothPriorities();

        assertThat(foeLiage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Nonland permanents do not trigger the ability")
    void nonlandEntryDoesNotTrigger() {
        Permanent foeLiage = harness.addToBattlefieldAndReturn(player1, new FoeLiage());

        harness.enterBattlefieldAndReturn(player1, new FoeLiage());

        assertThat(gd.stack).isEmpty();
        assertThat(foeLiage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each land entry creates a separate counter trigger")
    void multipleLandEntriesGiveMultipleCounters() {
        Permanent foeLiage = harness.addToBattlefieldAndReturn(player1, new FoeLiage());

        harness.enterBattlefieldAndReturn(player1, new CommandTower());
        harness.enterBattlefieldAndReturn(player2, new CommandTower());

        assertThat(gd.stack).hasSize(2);
        assertThat(foeLiage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        resolveAllTriggers();

        assertThat(foeLiage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each copy puts a counter only on itself")
    void eachCopyGetsItsOwnCounter() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new FoeLiage());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new FoeLiage());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new FoeLiage());

        harness.enterBattlefieldAndReturn(player2, new CommandTower());

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A land entering outside its controller's turn creates no trigger")
    void opponentsTurnDoesNotPutAbilityOnStack() {
        Permanent foeLiage = harness.addToBattlefieldAndReturn(player2, new FoeLiage());

        harness.enterBattlefieldAndReturn(player2, new CommandTower());

        assertThat(gd.stack).isEmpty();
        assertThat(foeLiage.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
