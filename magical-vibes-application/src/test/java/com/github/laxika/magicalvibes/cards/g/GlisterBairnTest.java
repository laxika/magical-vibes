package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GlisterBairnTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("At beginning of combat, boosts another creature by the number of colors among permanents you control")
    void boostsAnotherCreatureByControlledColors() {
        harness.addToBattlefield(player1, new GlisterBairn());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GoblinPiker());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target itself or a creature controlled by an opponent")
    void onlyTargetsAnotherCreatureYouControl() {
        Permanent bairn = harness.addToBattlefieldAndReturn(player1, new GlisterBairn());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(
                PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(ownCreature.getId())
                .doesNotContain(bairn.getId(), opponentCreature.getId());
    }

    @Test
    @DisplayName("Does not trigger during an opponent's combat")
    void doesNotTriggerDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new GlisterBairn());
        harness.addToBattlefield(player1, new GrizzlyBears());

        advanceToCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GlisterBairn());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        assertThat(target.getPowerModifier()).isEqualTo(2);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }
}
