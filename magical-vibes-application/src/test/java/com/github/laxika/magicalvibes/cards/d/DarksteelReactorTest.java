package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DarksteelReactorTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger may put a charge counter on Darksteel Reactor")
    void upkeepTriggerMayPutChargeCounter() {
        Permanent reactor = harness.addToBattlefieldAndReturn(player1, new DarksteelReactor());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(reactor.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the upkeep trigger does not add a charge counter")
    void decliningUpkeepTriggerDoesNotAddChargeCounter() {
        Permanent reactor = harness.addToBattlefieldAndReturn(player1, new DarksteelReactor());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(reactor.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Winning state trigger fires when the twentieth charge counter is placed")
    void winsWhenTwentiethChargeCounterIsPlaced() {
        Permanent reactor = harness.addToBattlefieldAndReturn(player1, new DarksteelReactor());
        reactor.setCounterCount(CounterType.CHARGE, 19);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Winning state trigger is not limited to the controller's upkeep")
    void winsDuringOpponentsUpkeep() {
        Permanent reactor = harness.addToBattlefieldAndReturn(player1, new DarksteelReactor());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        reactor.setCounterCount(CounterType.CHARGE, 20);

        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}
