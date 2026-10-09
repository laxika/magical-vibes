package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.EchoingRuin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarksteelReactor.class, EchoingRuin.class})
class DarksteelReactorTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent's upkeep does not trigger the optional charge counter ability")
    void opponentsUpkeepDoesNotAddCounter() {
        Permanent reactor = harness.addToBattlefieldAndReturn(player1, new DarksteelReactor());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(reactor.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Nineteen charge counters do not trigger a win")
    void belowThresholdDoesNotWin() {
        Permanent reactor = harness.addToBattlefieldAndReturn(player1, new DarksteelReactor());
        reactor.setCounterCount(CounterType.CHARGE, 19);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("More than twenty charge counters also trigger a win for the controller")
    void aboveThresholdWinsForController() {
        Permanent reactor = harness.addToBattlefieldAndReturn(player2, new DarksteelReactor());
        reactor.setCounterCount(CounterType.CHARGE, 21);

        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Removing counters after the win ability triggers does not prevent the win")
    void removingCountersAfterTriggerDoesNotPreventWin() {
        Permanent reactor = harness.addToBattlefieldAndReturn(player1, new DarksteelReactor());
        reactor.setCounterCount(CounterType.CHARGE, 20);

        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);
        reactor.setCounterCount(CounterType.CHARGE, 0);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Indestructibility prevents Echoing Ruin from destroying the Reactor")
    void survivesArtifactDestruction() {
        Permanent reactor = harness.addToBattlefieldAndReturn(player2, new DarksteelReactor());
        harness.setHand(player1, List.of(new EchoingRuin()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, reactor.getId());

        harness.assertOnBattlefield(player2, "Darksteel Reactor");
        harness.assertNotInGraveyard(player2, "Darksteel Reactor");
    }

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
