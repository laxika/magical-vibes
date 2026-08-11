package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GraveshifterTest extends BaseCardTest {

    private void castGraveshifter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Graveshifter()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns a targeted creature card from the graveyard to hand")
    void etbReturnsCreatureToHand() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        castGraveshifter();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB return can be declined")
    void returnCanBeDeclined() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        castGraveshifter();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Noncreature cards are not legal graveyard targets")
    void nonCreatureIsNotTargetable() {
        harness.setGraveyard(player1, List.of(new HolyDay()));

        castGraveshifter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Holy Day");
    }

    @Test
    @DisplayName("Empty graveyard produces no graveyard choice")
    void emptyGraveyardProducesNoChoice() {
        castGraveshifter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertOnBattlefield(player1, "Graveshifter");
    }
}
