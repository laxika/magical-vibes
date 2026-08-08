package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UncoveredCluesTest extends BaseCardTest {

    @Test
    @DisplayName("Offers a multi-pick of instant/sorcery cards among the top four, capped at two")
    void offersMultiPickCappedAtTwo() {
        setupTopFour(List.of(new Shock(), new GrizzlyBears(), new Shock(), new Forest()));
        castClues();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).hasSize(2);
    }

    @Test
    @DisplayName("Puts the two chosen instant/sorcery cards into hand")
    void putsTwoChosenCardsIntoHand() {
        Shock first = new Shock();
        Shock second = new Shock();
        setupTopFour(List.of(first, new GrizzlyBears(), second, new Forest()));
        castClues();

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Shock", "Shock");
    }

    @Test
    @DisplayName("Declining reveals keeps hand empty and bottoms all four looked-at cards")
    void decliningKeepsHandEmpty() {
        setupTopFour(List.of(new Shock(), new GrizzlyBears(), new Shock(), new Forest()));
        castClues();

        harness.handleMultipleCardsChosen(player1, List.of());

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(4);
    }

    @Test
    @DisplayName("With no instant or sorcery among the top four, nothing goes to hand")
    void noMatchingCardsPutsNothingIntoHand() {
        setupTopFour(List.of(new GrizzlyBears(), new Forest(), new GrizzlyBears(), new Forest()));
        castClues();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.LibraryRevealChoice.class);
    }

    @Test
    @DisplayName("With an empty library, Uncovered Clues does nothing")
    void emptyLibraryDoesNothing() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        castClues();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castClues() {
        harness.setHand(player1, List.of(new UncoveredClues()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void setupTopFour(List<Card> cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }
}
