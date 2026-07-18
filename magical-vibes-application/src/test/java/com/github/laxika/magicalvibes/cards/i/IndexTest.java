package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IndexTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Index enters library reorder state with 5 cards")
    void resolvingEntersLibraryReorderState() {
        harness.setHand(player1, List.of(new Index()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(5);
    }

    @Test
    @DisplayName("Reordering changes the order of the top cards of the library")
    void reorderingChangesTopCards() {
        harness.setHand(player1, List.of(new Index()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);
        Card top2 = deck.get(2);
        Card top3 = deck.get(3);
        Card top4 = deck.get(4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(4, 3, 2, 1, 0)));

        assertThat(deck.get(0)).isSameAs(top4);
        assertThat(deck.get(1)).isSameAs(top3);
        assertThat(deck.get(2)).isSameAs(top2);
        assertThat(deck.get(3)).isSameAs(top1);
        assertThat(deck.get(4)).isSameAs(top0);
    }

    @Test
    @DisplayName("Completing the reorder clears the interaction and Index goes to graveyard")
    void completingReorderResolvesSpell() {
        harness.setHand(player1, List.of(new Index()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2, 3, 4)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Index"));
    }
}
