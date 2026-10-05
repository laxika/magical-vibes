package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SeizeOpportunity;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MonasteryMessenger.class, GrizzlyBears.class, Island.class, Shock.class, SeizeOpportunity.class})
class MonasteryMessengerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers only noncreature, nonland cards from its controller's graveyard")
    void etbOffersOnlyNoncreatureNonlandCards() {
        Shock shock = new Shock();
        GrizzlyBears creature = new GrizzlyBears();
        Island land = new Island();
        harness.setGraveyard(player1, List.of(shock, creature, land));

        castMessenger();

        List<UUID> validIds = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds();
        assertThat(validIds).containsExactly(shock.getId());
    }

    @Test
    @DisplayName("ETB puts the chosen card on top of its controller's library")
    void etbPutsChosenCardOnTopOfLibrary() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setLibrary(player1, List.of(new Island()));

        castMessenger();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(shock);
        harness.assertNotInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("ETB can choose no card")
    void etbCanChooseNoCard() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setLibrary(player1, List.of());

        castMessenger();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB needs no target selection when there is no legal graveyard target")
    void etbNeedsNoSelectionWithoutLegalTarget() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Island()));

        castMessenger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNull();
    }

    @Test
    @DisplayName("ETB excludes cards in the opponent's graveyard")
    void etbExcludesOpponentsGraveyard() {
        SeizeOpportunity ownCard = new SeizeOpportunity();
        SeizeOpportunity opponentCard = new SeizeOpportunity();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));

        castMessenger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(ownCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
    }

    @Test
    @DisplayName("ETB returns only the selected card and preserves the library order")
    void etbReturnsOnlySelectedCard() {
        SeizeOpportunity selected = new SeizeOpportunity();
        SeizeOpportunity unselected = new SeizeOpportunity();
        Island top = new Island();
        Island bottom = new Island();
        harness.setGraveyard(player1, List.of(unselected, selected));
        harness.setLibrary(player1, List.of(top, bottom));

        castMessenger();
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(selected, top, bottom);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unselected);
        harness.assertNotInHand(player1, "Seize Opportunity");
    }

    @Test
    @DisplayName("ETB does not choose a replacement when its target leaves the graveyard")
    void etbDoesNotRetargetAtResolution() {
        SeizeOpportunity selected = new SeizeOpportunity();
        SeizeOpportunity remaining = new SeizeOpportunity();
        Island top = new Island();
        harness.setGraveyard(player1, List.of(selected, remaining));
        harness.setLibrary(player1, List.of(top));

        castMessenger();
        harness.handleMultipleCardsChosen(player1, List.of(selected.getId()));
        harness.setGraveyard(player1, List.of(remaining));
        harness.setExile(player1, List.of(selected));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    private void castMessenger() {
        harness.setHand(player1, List.of(new MonasteryMessenger()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
