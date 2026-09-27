package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.o.OneWithNothing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MeasureOfWickedness.class, OneWithNothing.class})
class MeasureOfWickednessTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and its controller loses 8 life at that player's end step")
    void sacrificesAndLosesLifeAtControllerEndStep() {
        harness.addToBattlefield(player1, new MeasureOfWickedness());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Measure of Wickedness");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 8);
    }

    @Test
    @DisplayName("Another card entering its controller's graveyard gives the enchantment to a target opponent")
    void anotherCardInGraveyardGivesControlToTargetOpponent() {
        harness.addToBattlefield(player1, new MeasureOfWickedness());
        harness.setHand(player1, List.of(new OneWithNothing()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Measure of Wickedness");
        harness.assertNotOnBattlefield(player1, "Measure of Wickedness");
    }

    @Test
    @DisplayName("Does not sacrifice itself during an opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new MeasureOfWickedness());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Measure of Wickedness");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Ignores a card put into an opponent's graveyard")
    void ignoresCardPutIntoOpponentsGraveyard() {
        harness.addToBattlefield(player1, new MeasureOfWickedness());
        harness.setHand(player2, List.of(new OneWithNothing()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player2, 0);

        harness.assertOnBattlefield(player1, "Measure of Wickedness");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("After changing control, watches the new controller's graveyard")
    void watchesNewControllersGraveyardAfterControlChange() {
        harness.addToBattlefield(player1, new MeasureOfWickedness());
        harness.setHand(player1, List.of(new OneWithNothing()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new OneWithNothing()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player1.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Measure of Wickedness");
        harness.assertNotOnBattlefield(player2, "Measure of Wickedness");
    }
}
