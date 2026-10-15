package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AncestralReminiscence.class, GrizzlyBears.class, Island.class})
class AncestralReminiscenceTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards, then makes the controller discard a card")
    void drawsThreeThenDiscardsOne() {
        harness.setLibrary(player1, new ArrayList<>(List.of(new Island(), new Island(), new Island())));
        harness.setHand(player1, List.of(new AncestralReminiscence(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Ancestral Reminiscence");
    }

    @Test
    @DisplayName("With no other cards in hand, can discard a card just drawn")
    void discardsNewlyDrawnCardFromInitiallyEmptyHand() {
        Island discardedCard = new Island();
        Island keptCardOne = new Island();
        Island keptCardTwo = new Island();
        harness.setLibrary(player1, List.of(discardedCard, keptCardOne, keptCardTwo));
        harness.setHand(player1, List.of(new AncestralReminiscence()));
        harness.setHand(player2, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(discardedCard, keptCardOne, keptCardTwo);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(keptCardOne, keptCardTwo);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discardedCard);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Ancestral Reminiscence");
    }
}
