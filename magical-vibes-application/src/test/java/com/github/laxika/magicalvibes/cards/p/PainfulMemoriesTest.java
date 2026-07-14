package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PainfulMemoriesTest extends BaseCardTest {

    private void castPainfulMemories() {
        harness.setHand(player1, List.of(new PainfulMemories()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Choosing a card puts it on top of the opponent's library")
    void choosingCardPutsItOnTop() {
        Card card1 = new GrizzlyBears();
        Card card2 = new PainfulMemories();
        harness.setHand(player2, new ArrayList<>(List.of(card1, card2)));

        castPainfulMemories();

        // Only one card is chosen.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();

        List<Card> deck = gd.playerDecks.get(player2.getId());
        assertThat(deck.get(0).getName()).isEqualTo("Grizzly Bears");

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).get(0).getName()).isEqualTo("Painful Memories");
    }

    @Test
    @DisplayName("Resolving against an empty hand does nothing")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());

        castPainfulMemories();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new PainfulMemories()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Goes to the caster's graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        castPainfulMemories();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Painful Memories"));
    }
}
