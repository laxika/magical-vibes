package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.v.VoraciousTyphon;
import com.github.laxika.magicalvibes.cards.r.RenegadeFreighter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StampedeRider.class, VoraciousTyphon.class, RenegadeFreighter.class})
class StampedeRiderTest extends BaseCardTest {

    private void advanceToCombatAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gets +1/+1 at the beginning of combat when you control a creature with power 4 or greater")
    void getsBoostForPowerFourCreature() {
        Permanent rider = addCreatureReady(player1, new StampedeRider());
        addCreatureReady(player1, new VoraciousTyphon());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not get +1/+1 when only an opponent controls a creature with power 4 or greater")
    void opponentCreatureDoesNotQualify() {
        Permanent rider = addCreatureReady(player1, new StampedeRider());
        addCreatureReady(player2, new VoraciousTyphon());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost wears off at the end of the turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent rider = addCreatureReady(player1, new StampedeRider());
        addCreatureReady(player1, new VoraciousTyphon());

        advanceToCombatAndResolve(player1);
        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(3);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(3);
    }

    @Test
    @DisplayName("Triggers during an opponent's combat as well")
    void triggersDuringOpponentCombat() {
        Permanent rider = addCreatureReady(player1, new StampedeRider());
        addCreatureReady(player1, new VoraciousTyphon());

        advanceToCombatAndResolve(player2);

        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(3);
    }

    @Test
    void doesNotTriggerWithoutQualifyingCreature() {
        Permanent rider = addCreatureReady(player1, new StampedeRider());

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(2);
    }

    @Test
    void rechecksPowerWhenTriggerResolves() {
        Permanent rider = addCreatureReady(player1, new StampedeRider());
        Permanent typhon = addCreatureReady(player1, new VoraciousTyphon());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);

        typhon.setPowerModifier(-1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(3);
    }

    @Test
    void riderCanQualifyUsingItsOwnModifiedPower() {
        Permanent rider = addCreatureReady(player1, new StampedeRider());
        rider.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        advanceToCombatAndResolve(player1);

        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(6);
    }

    @Test
    void uncrewedVehicleDoesNotQualify() {
        Permanent rider = addCreatureReady(player1, new StampedeRider());
        harness.addToBattlefield(player1, new RenegadeFreighter());

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(2);
    }
}
