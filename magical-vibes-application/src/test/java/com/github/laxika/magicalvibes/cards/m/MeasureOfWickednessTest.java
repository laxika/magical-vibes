package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MeasureOfWickednessTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and its controller loses 8 life at that player's end step")
    void sacrificesAndLosesLifeAtControllerEndStep() {
        harness.addToBattlefield(player1, new MeasureOfWickedness());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Measure of Wickedness");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 8);
    }

    @Test
    @DisplayName("Another card entering its controller's graveyard gives the enchantment to a target opponent")
    void anotherCardInGraveyardGivesControlToTargetOpponent() {
        harness.addToBattlefield(player1, new MeasureOfWickedness());
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Measure of Wickedness");
        harness.assertNotOnBattlefield(player1, "Measure of Wickedness");
    }
}
