package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GildedLight;
import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForgottenAncient.class, GildedLight.class, ScornfulEgotist.class})
class ForgottenAncientTest extends BaseCardTest {

    @Test
    void addsACounterWhenAnyPlayerCastsASpellAndTheTriggerIsAccepted() {
        Permanent ancient = addCreatureReady(player1, new ForgottenAncient());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GildedLight(), "{1}{W}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(ancient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void movesChosenCountersAmongOtherCreatures() {
        Permanent ancient = addCreatureReady(player1, new ForgottenAncient());
        Permanent ownCreature = addCreatureReady(player1, new ScornfulEgotist());
        Permanent opposingCreature = addCreatureReady(player2, new ScornfulEgotist());
        ancient.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 3);
        harness.handleXValueChosen(player1, 1);
        harness.handleXValueChosen(player1, 2);

        assertThat(ancient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void choosingZeroLeavesCountersOnTheAncient() {
        Permanent ancient = addCreatureReady(player1, new ForgottenAncient());
        addCreatureReady(player1, new ScornfulEgotist());
        ancient.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 0);

        assertThat(ancient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotAddACounterWhenTheSpellCastMayAbilityIsDeclined() {
        Permanent ancient = addCreatureReady(player1, new ForgottenAncient());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GildedLight(), "{1}{W}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(ancient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void triggersWhenTheAncientsControllerCastsASpell() {
        Permanent ancient = addCreatureReady(player1, new ForgottenAncient());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new GildedLight(), "{1}{W}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(ancient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotMoveCountersWhenTheUpkeepMayAbilityIsDeclined() {
        Permanent ancient = addCreatureReady(player1, new ForgottenAncient());
        Permanent otherCreature = addCreatureReady(player1, new ScornfulEgotist());
        ancient.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(ancient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canMoveOnlyPartOfTheChosenCountersToOneOtherCreature() {
        Permanent ancient = addCreatureReady(player1, new ForgottenAncient());
        Permanent otherCreature = addCreatureReady(player1, new ScornfulEgotist());
        ancient.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 1);
        harness.handleXValueChosen(player1, 1);

        assertThat(ancient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canSkipOwnCreatureAndMoveAllCountersToOpposingCreature() {
        Permanent ancient = addCreatureReady(player1, new ForgottenAncient());
        Permanent ownCreature = addCreatureReady(player1, new ScornfulEgotist());
        Permanent opposingCreature = addCreatureReady(player2, new ScornfulEgotist());
        ancient.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleXValueChosen(player1, 3);
        harness.handleXValueChosen(player1, 0);
        harness.handleXValueChosen(player1, 3);

        assertThat(ancient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void keepsCountersWhenThereAreNoOtherCreatures() {
        Permanent ancient = addCreatureReady(player1, new ForgottenAncient());
        ancient.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ancient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void finishesUpkeepAbilityWhenThereAreNoCountersToMove() {
        Permanent ancient = addCreatureReady(player1, new ForgottenAncient());
        Permanent otherCreature = addCreatureReady(player1, new ScornfulEgotist());

        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(ancient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotTriggerCounterMovementDuringOpponentsUpkeep() {
        Permanent ancient = addCreatureReady(player1, new ForgottenAncient());
        Permanent otherCreature = addCreatureReady(player2, new ScornfulEgotist());
        ancient.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        gd.turnNumber = 2;
        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(ancient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void spellCastTriggerGoesOnStackBeforeItsOptionalChoiceIsMade() {
        Permanent ancient = addCreatureReady(player1, new ForgottenAncient());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GildedLight(), "{1}{W}");

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(ancient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(ancient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player2, "Gilded Light");
    }
}
