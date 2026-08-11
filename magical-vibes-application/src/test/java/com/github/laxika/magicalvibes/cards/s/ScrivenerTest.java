package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScrivenerTest extends BaseCardTest {

    private void castScrivener() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Scrivener()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB returns the chosen instant card from the graveyard")
    void returnsChosenInstantToHand() {
        HolyDay holyDay = new HolyDay();
        Unsummon unsummon = new Unsummon();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), holyDay, unsummon));

        castScrivener();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(holyDay.getId(), unsummon.getId());

        harness.handleMultipleCardsChosen(player1, List.of(unsummon.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Unsummon");
        harness.assertInGraveyard(player1, "Holy Day");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The optional return can be declined")
    void returnCanBeDeclined() {
        HolyDay holyDay = new HolyDay();
        harness.setGraveyard(player1, List.of(holyDay));

        castScrivener();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Holy Day");
        harness.assertNotInHand(player1, "Holy Day");
    }

    @Test
    @DisplayName("A non-instant card is not a legal target")
    void nonInstantIsNotTargetable() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        castScrivener();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("An empty graveyard produces no target choice")
    void emptyGraveyardProducesNoChoice() {
        castScrivener();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
