package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.Mistwalker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DraugrThoughtThief.class, Mistwalker.class})
class DraugrThoughtThiefTest extends BaseCardTest {

    private void castDraugrThoughtThief() {
        harness.castFromHand(player1, new DraugrThoughtThief(), "{2}{U}");
    }

    @Test
    @DisplayName("ETB target selection offers both the controller and opponent")
    void targetFilterIncludesAllPlayers() {
        castDraugrThoughtThief();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(player1.getId(), player2.getId());
    }

    @Test
    @DisplayName("Controller may put the target opponent's top card into their graveyard")
    void putsTopCardIntoTargetGraveyardWhenAccepted() {
        Card topCard = new Mistwalker();
        gd.playerDecks.get(player2.getId()).add(0, topCard);

        castDraugrThoughtThief();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Declining leaves the target opponent's top card on their library")
    void leavesTopCardWhenDeclined() {
        Card topCard = new Mistwalker();
        gd.playerDecks.get(player2.getId()).add(0, topCard);
        int deckBefore = gd.playerDecks.get(player2.getId()).size();

        castDraugrThoughtThief();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckBefore);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    @DisplayName("Controller can target their own library and put its top card into their graveyard")
    void canPutOwnTopCardIntoGraveyard() {
        Card topCard = new Mistwalker();
        harness.setLibrary(player1, List.of(topCard));

        castDraugrThoughtThief();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("An empty target library resolves without a graveyard choice")
    void emptyLibraryDoesNotOfferChoice() {
        harness.setLibrary(player2, List.of());

        castDraugrThoughtThief();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Draugr Thought-Thief");
    }
}
