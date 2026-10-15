package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DawnhartWardens.class, GrizzlyBears.class, LlanowarElves.class})
class DawnhartWardensTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    private void endTurn() {
        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Coven gives your creatures +1/+0 at the beginning of combat")
    void covenBoostsYourCreatures() {
        Permanent wardens = harness.addToBattlefieldAndReturn(player1, new DawnhartWardens());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(wardens.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(elves.getPowerModifier()).isEqualTo(1);
        assertThat(opponentCreature.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Coven does not trigger without three different powers")
    void covenRequiresThreeDifferentPowers() {
        Permanent wardens = harness.addToBattlefieldAndReturn(player1, new DawnhartWardens());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(wardens.getPowerModifier()).isZero();
        assertThat(bears.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Coven boost wears off at end of turn")
    void covenBoostWearsOffAtEndOfTurn() {
        Permanent wardens = harness.addToBattlefieldAndReturn(player1, new DawnhartWardens());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());

        advanceToCombat(player1);
        harness.passBothPriorities();
        assertThat(wardens.getPowerModifier()).isEqualTo(1);

        endTurn();

        assertThat(wardens.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Coven does not trigger during an opponent's combat")
    void doesNotTriggerOnOpponentsTurn() {
        Permanent wardens = harness.addToBattlefieldAndReturn(player1, new DawnhartWardens());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(wardens.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Coven is checked again when the trigger resolves")
    void losingDistinctPowerPreventsBoost() {
        Permanent wardens = harness.addToBattlefieldAndReturn(player1, new DawnhartWardens());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(1);
        elves.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(wardens.getPowerModifier()).isZero();
        assertThat(bears.getPowerModifier()).isZero();
        assertThat(elves.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent creatures cannot supply missing coven powers")
    void opponentsCreaturesDoNotCountForCoven() {
        Permanent wardens = harness.addToBattlefieldAndReturn(player1, new DawnhartWardens());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(wardens.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The boost includes creatures present at resolution, but not later arrivals")
    void boostUsesCreaturesPresentAtResolution() {
        Permanent wardens = harness.addToBattlefieldAndReturn(player1, new DawnhartWardens());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());

        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(1);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(wardens.getPowerModifier()).isEqualTo(1);
        assertThat(beforeResolution.getPowerModifier()).isEqualTo(1);
        assertThat(beforeResolution.getToughnessModifier()).isZero();
        assertThat(afterResolution.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Meeting coven after combat begins does not create a trigger")
    void meetingCovenTooLateDoesNotTrigger() {
        Permanent wardens = harness.addToBattlefieldAndReturn(player1, new DawnhartWardens());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToCombat(player1);
        assertThat(gd.stack).isEmpty();
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(wardens.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("Coven uses modified powers, not just printed powers")
    void modifiedPowersEnableCoven() {
        Permanent wardens = harness.addToBattlefieldAndReturn(player1, new DawnhartWardens());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent smallerBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        smallerBears.setPowerModifier(-1);

        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(wardens.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(smallerBears.getPowerModifier()).isZero();
    }
}
