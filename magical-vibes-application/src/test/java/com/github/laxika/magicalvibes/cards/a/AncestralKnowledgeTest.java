package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AncestralKnowledgeTest extends BaseCardTest {

    private List<Card> castAndLookAtFourCards() {
        harness.setHand(player1, List.of(new AncestralKnowledge()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Card c0 = new Island();
        Card c1 = new Forest();
        Card c2 = new Mountain();
        Card c3 = new GrizzlyBears();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(c0, c1, c2, c3));

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // enchantment resolves, ETB trigger goes on the stack
        harness.passBothPriorities(); // ETB trigger resolves

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        return List.of(c0, c1, c2, c3);
    }

    @Test
    @DisplayName("Exiles any number of the looked-at cards, then puts the rest back on top in the chosen order")
    void exilesAnyNumberRestOnTop() {
        List<Card> cards = castAndLookAtFourCards();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(1));

        // The pick repeats over what is left instead of ending after one exile.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .contains(cards.get(1).getId(), cards.get(0).getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        List<Card> deckAfter = gd.playerDecks.get(player1.getId());
        assertThat(deckAfter).hasSize(2);
        assertThat(deckAfter.get(0).getId()).isEqualTo(cards.get(3).getId());
        assertThat(deckAfter.get(1).getId()).isEqualTo(cards.get(2).getId());
    }

    @Test
    @DisplayName("Exiling nothing puts every looked-at card back on top")
    void mayExileNothing() {
        List<Card> cards = castAndLookAtFourCards();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        List<Card> deckAfter = gd.playerDecks.get(player1.getId());
        assertThat(deckAfter).hasSize(4);
        assertThat(deckAfter.get(0).getId()).isEqualTo(cards.get(3).getId());
        assertThat(deckAfter.get(3).getId()).isEqualTo(cards.get(0).getId());
    }

    @Test
    @DisplayName("Leaving the battlefield shuffles the controller's library")
    void leavingBattlefieldShufflesLibrary() {
        Permanent knowledge = harness.addToBattlefieldAndReturn(player1, new AncestralKnowledge());

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castInstant(player2, 0, knowledge.getId());
        harness.passBothPriorities(); // Disenchant
        harness.passBothPriorities(); // leaves-the-battlefield shuffle trigger

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(knowledge);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains(player1.getUsername() + " shuffles their library."));
    }

    @Test
    @DisplayName("Declining the cumulative upkeep sacrifices it and shuffles the library")
    void decliningCumulativeUpkeepSacrifices() {
        Permanent knowledge = harness.addToBattlefieldAndReturn(player1, new AncestralKnowledge());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // cumulative upkeep trigger
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(knowledge);
        harness.assertInGraveyard(player1, "Ancestral Knowledge");
    }
}
