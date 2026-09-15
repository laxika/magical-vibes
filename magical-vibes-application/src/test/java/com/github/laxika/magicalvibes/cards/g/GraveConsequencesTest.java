package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GraveConsequences.class)
class GraveConsequencesTest extends BaseCardTest {

    @Test
    @DisplayName("Each player chooses graveyard exiles in APNAP order, loses for the remainder, and the controller draws")
    void eachPlayerChoosesAndLosesForRemainingGraveyardCards() {
        Card ownFirst = new GraveConsequences();
        Card ownSecond = new GraveConsequences();
        Card opponentCard = new GraveConsequences();
        Card drawn = new GraveConsequences();

        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(ownFirst, ownSecond));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setLibrary(player1, List.of(drawn));
        harness.castFromHand(player1, new GraveConsequences(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .playerId()).isEqualTo(player1.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownFirst.getId()));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .playerId()).isEqualTo(player2.getId());
        harness.handleMultipleCardsChosen(player2, List.of());

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownFirst);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownSecond);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentCard);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Exiling every card from both graveyards prevents the life loss")
    void exilingEveryCardPreventsLifeLoss() {
        Card ownCard = new GraveConsequences();
        Card opponentCard = new GraveConsequences();

        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.castFromHand(player1, new GraveConsequences(), "{1}{B}");
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(ownCard.getId()));
        harness.handleMultipleCardsChosen(player2, List.of(opponentCard.getId()));

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentCard);
    }

    @Test
    @DisplayName("Life loss counts every card left in each graveyard")
    void lifeLossCountsEveryRemainingGraveyardCard() {
        Card ownFirst = new GraveConsequences();
        Card ownSecond = new GraveConsequences();
        Card ownThird = new GraveConsequences();
        Card opponentFirst = new GraveConsequences();
        Card opponentSecond = new GraveConsequences();
        Card drawn = new GraveConsequences();
        GraveConsequences spell = new GraveConsequences();

        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(ownFirst, ownSecond, ownThird));
        harness.setGraveyard(player2, List.of(opponentFirst, opponentSecond));
        harness.setLibrary(player1, List.of(drawn));
        harness.castFromHand(player1, spell, "{1}{B}");
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(ownFirst.getId()));
        harness.handleMultipleCardsChosen(player2, List.of(opponentFirst.getId()));

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 19);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownSecond, ownThird, spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentSecond);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Empty graveyards do not create choices and the spell still draws")
    void emptyGraveyardsStillAllowTheDraw() {
        Card drawn = new GraveConsequences();

        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(drawn));
        harness.castFromHand(player1, new GraveConsequences(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("The active player makes the first graveyard choice")
    void activePlayerChoosesFirst() {
        Card ownCard = new GraveConsequences();
        Card opponentCard = new GraveConsequences();
        Card drawn = new GraveConsequences();

        harness.forceActivePlayer(player2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setLibrary(player2, List.of(drawn));
        harness.castFromHand(player2, new GraveConsequences(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .playerId()).isEqualTo(player2.getId());
        harness.handleMultipleCardsChosen(player2, List.of());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .playerId()).isEqualTo(player1.getId());
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        assertThat(gd.playerHands.get(player2.getId())).contains(drawn);
    }
}
