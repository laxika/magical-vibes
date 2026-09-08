package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestamentBearerTest extends BaseCardTest {

    @Test
    @DisplayName("When Testament Bearer dies, one of the top three cards goes to hand and the rest go to the graveyard")
    void deathTriggerChoosesOneCardAndGraveyardsTheRest() {
        Permanent bearer = harness.addToBattlefieldAndReturn(player1, new TestamentBearer());
        Card chosen = new GrizzlyBears();
        Card restOne = new Shock();
        Card restTwo = new Shock();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(chosen, restOne, restTwo));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bearer));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(bearer.getCard(), restOne, restTwo);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With fewer than three cards in the library, all available cards go to hand")
    void shortLibraryPutsAvailableCardsIntoHand() {
        Permanent bearer = harness.addToBattlefieldAndReturn(player1, new TestamentBearer());
        Card onlyCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(onlyCard));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bearer));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bearer.getCard());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
