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
        harness.clearPriorityPassed();
        harness.passBothPriorities();
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
}
