package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SabertoothMauler.class})
class SabertoothMaulerTest extends BaseCardTest {

    @Test
    @DisplayName("At your end step, a creature death adds a counter and untaps Sabertooth Mauler")
    void creatureDeathAddsCounterAndUntaps() {
        Permanent mauler = addReadyMauler();
        mauler.tap();
        gd.creatureDeathCountThisTurn.put(player2.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(mauler.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does nothing at your end step when no creature died this turn")
    void noCreatureDeathDoesNothing() {
        Permanent mauler = addReadyMauler();
        mauler.tap();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(mauler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Multiple actual creature deaths produce only one counter")
    void multipleDeathsProduceOneCounter() {
        Permanent mauler = addReadyMauler();
        mauler.tap();
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SabertoothMauler());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new SabertoothMauler());
        harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, ownCreature);
        harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, opposingCreature);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(mauler.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A creature death does not trigger Mauler during the opponent's end step")
    void opponentsEndStepDoesNotTrigger() {
        Permanent mauler = addReadyMauler();
        mauler.tap();
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(mauler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An untapped Mauler still receives a counter")
    void untappedMaulerReceivesCounter() {
        Permanent mauler = addReadyMauler();
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(mauler.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A death after the end step begins cannot create the missed trigger")
    void deathAfterEndStepBeginsDoesNotTrigger() {
        Permanent mauler = addReadyMauler();
        mauler.tap();
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new SabertoothMauler());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, opposingCreature);

        assertThat(gd.stack).isEmpty();
        assertThat(mauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(mauler.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A pending trigger cannot affect a different Mauler after its source dies")
    void sourceDiesBeforeResolution() {
        Permanent mauler = addReadyMauler();
        gd.creatureDeathCountThisTurn.put(player1.getId(), 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, mauler);
        Permanent otherMauler = addReadyMauler();
        otherMauler.tap();
        harness.passBothPriorities();

        assertThat(otherMauler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherMauler.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyMauler() {
        return harness.addToBattlefieldAndReturn(player1, new SabertoothMauler());
    }
}
