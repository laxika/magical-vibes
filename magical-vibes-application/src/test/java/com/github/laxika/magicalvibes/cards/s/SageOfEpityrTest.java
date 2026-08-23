package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SageOfEpityr.class)
class SageOfEpityrTest extends BaseCardTest {

    @Test
    @DisplayName("Sage of Epityr's enters-the-battlefield ability offers the top four cards for reorder")
    void entersTheBattlefieldAbilityOffersTopFourCardsForReorder() {
        harness.setHand(player1, List.of(new SageOfEpityr()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.playerId()).isEqualTo(player1.getId());
        assertThat(reorder.cards()).hasSize(4);
    }

    @Test
    @DisplayName("Sage of Epityr's enters-the-battlefield ability changes the top-card order")
    void entersTheBattlefieldAbilityChangesTopCardOrder() {
        harness.setHand(player1, List.of(new SageOfEpityr()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        GameData gameData = harness.getGameData();
        List<Card> deck = gameData.playerDecks.get(player1.getId());
        Card originalTop = deck.get(3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.getGameService().handleInteractionAnswer(
                gameData, player1, new InteractionAnswer.CardOrder(List.of(3, 0, 1, 2)));

        assertThat(deck.getFirst()).isSameAs(originalTop);
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sage of Epityr reorders all cards when the library has fewer than four")
    void reordersAllCardsInShortLibrary() {
        harness.setHand(player1, List.of(new SageOfEpityr()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        Card cardA = new SageOfEpityr();
        Card cardB = new SageOfEpityr();
        deck.addAll(List.of(cardA, cardB));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactly(cardA, cardB);

        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(deck).containsExactly(cardB, cardA);
    }

    @Test
    @DisplayName("Sage of Epityr skips reordering when the library is empty")
    void skipsReorderingForEmptyLibrary() {
        harness.setHand(player1, List.of(new SageOfEpityr()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        gd.playerDecks.get(player1.getId()).clear();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
