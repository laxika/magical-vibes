package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GraveConsequences.class, AirElemental.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class GraveConsequencesTest extends BaseCardTest {

    @Test
    @DisplayName("Each player chooses graveyard exiles in APNAP order, loses for the remainder, and the controller draws")
    void eachPlayerChoosesAndLosesForRemainingGraveyardCards() {
        Card ownFirst = new GrizzlyBears();
        Card ownSecond = new HillGiant();
        Card opponentCard = new AirElemental();
        Forest drawn = new Forest();

        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(ownFirst, ownSecond));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setLibrary(player1, List.of(drawn));
        harness.setHand(player1, List.of(new GraveConsequences()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0);
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
        Card ownCard = new GrizzlyBears();
        Card opponentCard = new HillGiant();

        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new GraveConsequences()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(ownCard.getId()));
        harness.handleMultipleCardsChosen(player2, List.of(opponentCard.getId()));

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentCard);
    }
}
