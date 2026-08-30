package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DebtorsKnell.class, GrizzlyBears.class, HolyDay.class})
class DebtorsKnellTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature card from any graveyard under its controller's control")
    void returnsCreatureFromAnyGraveyardUnderItsControllerControl() {
        harness.addToBattlefield(player1, new DebtorsKnell());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        advanceToUpkeep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(bears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only creature cards are legal upkeep targets")
    void onlyCreatureCardsAreLegalTargets() {
        harness.addToBattlefield(player1, new DebtorsKnell());
        harness.setGraveyard(player2, List.of(new HolyDay()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Holy Day");
    }
}
