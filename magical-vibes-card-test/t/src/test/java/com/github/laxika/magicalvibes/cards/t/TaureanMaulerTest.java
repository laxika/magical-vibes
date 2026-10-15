package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.p.PricklyBoggart;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TaureanMauler.class, PricklyBoggart.class, Bitterblossom.class})
class TaureanMaulerTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent casting a spell triggers may ability and accepting adds a counter")
    void opponentSpellAcceptedAddsCounter() {
        Permanent mauler = harness.addToBattlefieldAndReturn(player1, new TaureanMauler());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.castFromHand(player2, new PricklyBoggart(), "{B}");

        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).hasSize(1);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, mauler)).isEqualTo(3);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, mauler)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining the may ability does not add a counter")
    void opponentSpellDeclinedDoesNotAddCounter() {
        Permanent mauler = harness.addToBattlefieldAndReturn(player1, new TaureanMauler());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new PricklyBoggart(), "{B}");

        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).hasSize(1);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Opponent casting a noncreature spell also triggers the may ability")
    void opponentNoncreatureSpellTriggers() {
        Permanent mauler = harness.addToBattlefieldAndReturn(player1, new TaureanMauler());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new Bitterblossom(), "{1}{B}");

        harness.passBothPriorities();

        assertThat(gd.pendingMayAbilities).hasSize(1);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Controller casting a spell does not trigger Taurean Mauler")
    void controllerSpellDoesNotTrigger() {
        Permanent mauler = harness.addToBattlefieldAndReturn(player1, new TaureanMauler());

        harness.castFromHand(player1, new PricklyBoggart(), "{B}");

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The optional counter choice waits until the trigger resolves")
    void counterChoiceWaitsForResolution() {
        Permanent mauler = harness.addToBattlefieldAndReturn(player1, new TaureanMauler());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new PricklyBoggart(), "{B}");

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).hasSize(1);
        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Prickly Boggart");
    }

    @Test
    @DisplayName("Each opposing spell can add another counter")
    void repeatedOpponentSpellsAddCounters() {
        Permanent mauler = harness.addToBattlefieldAndReturn(player1, new TaureanMauler());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new PricklyBoggart(), "{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.castFromHand(player2, new Bitterblossom(), "{1}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Only the opposing player's Mauler gets a counter")
    void countersGoToTheCorrectSource() {
        Permanent opposingMauler = harness.addToBattlefieldAndReturn(player1, new TaureanMauler());
        Permanent ownMauler = harness.addToBattlefieldAndReturn(player2, new TaureanMauler());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new PricklyBoggart(), "{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(opposingMauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownMauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

}
