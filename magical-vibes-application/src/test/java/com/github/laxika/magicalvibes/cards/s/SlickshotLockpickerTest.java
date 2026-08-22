package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlickshotLockpicker.class, Shock.class, GrizzlyBears.class})
class SlickshotLockpickerTest extends BaseCardTest {

    @Test
    void entersAndOnlyTargetsInstantOrSorceryInControllerGraveyard() {
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(shock, bears));
        castLockpicker();

        List<java.util.UUID> validIds = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds();

        assertThat(validIds).containsExactly(shock.getId());
    }

    @Test
    void grantsFlashbackAndExilesTheCastCard() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        castLockpicker();

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shock);
    }

    @Test
    void doesNotPromptWithoutAValidGraveyardTarget() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        castLockpicker();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    void canBePlottedForItsPlotCost() {
        SlickshotLockpicker lockpicker = new SlickshotLockpicker();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(lockpicker));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, List.of());

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(lockpicker);
        assertThat(gd.plottedCardIds).contains(lockpicker.getId());
    }

    private void castLockpicker() {
        harness.setHand(player1, List.of(new SlickshotLockpicker()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
