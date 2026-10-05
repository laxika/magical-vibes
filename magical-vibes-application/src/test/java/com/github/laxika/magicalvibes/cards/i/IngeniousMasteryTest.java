package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IngeniousMastery.class})
class IngeniousMasteryTest extends BaseCardTest {

    @Test
    @DisplayName("Normal casting draws X cards")
    void normalCastDrawsXCards() {
        harness.setHand(player1, List.of(new IngeniousMastery()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Alternate casting draws three, gives the opponent Treasures, and makes them scry")
    void alternateCastDrawsThreeAndMakesOpponentScry() {
        harness.setHand(player1, List.of(new IngeniousMastery()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(findPermanents(player2, "Treasure")).hasSize(2);

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry.playerId()).isEqualTo(player2.getId());
        assertThat(scry.libraryOwnerId()).isEqualTo(player2.getId());

        harness.getGameService().handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Paying the normal cost with X zero does not grant alternate-cost benefits")
    void normalCastWithZeroDrawsNothing() {
        harness.setHand(player1, List.of(new IngeniousMastery()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Alternate casting lets the opponent bottom their only library card")
    void alternateCastScriesOneCardWhenOpponentLibraryHasOneCard() {
        IngeniousMastery libraryCard = new IngeniousMastery();
        harness.setLibrary(player2, List.of(libraryCard));
        harness.setHand(player1, List.of(new IngeniousMastery()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(findPermanents(player2, "Treasure")).hasSize(2);
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry.playerId()).isEqualTo(player2.getId());
        gs.handleInteractionAnswer(gd, player2,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty opponent library does not prevent alternate-cost draws or Treasures")
    void alternateCastWithEmptyOpponentLibrary() {
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new IngeniousMastery()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(findPermanents(player2, "Treasure")).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
