package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UncoveredClues.class, Shock.class, GrizzlyBears.class, Forest.class})
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
        harness.setLibrary(player1, List.of());
        castClues();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May choose only one sorcery when more than two spells are eligible")
    void choosesOneSorceryAndOrdersRestBelowUntouchedLibrary() {
        Shock first = new Shock();
        UncoveredClues chosen = new UncoveredClues();
        Shock third = new Shock();
        Forest fourth = new Forest();
        Shock untouched = new Shock();
        harness.setLibrary(player1, List.of(first, chosen, third, fourth, untouched));
        castClues();

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, fourth, first, third);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May decline the only eligible spell in a library shorter than four cards")
    void declinesOnlySpellInShortLibrary() {
        UncoveredClues sorcery = new UncoveredClues();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(sorcery, land));
        castClues();

        harness.handleMultipleCardsChosen(player1, List.of());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, sorcery);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Can select an instant and a sorcery together from the top four")
    void selectsInstantAndSorceryTogether() {
        Shock instant = new Shock();
        UncoveredClues sorcery = new UncoveredClues();
        GrizzlyBears creature = new GrizzlyBears();
        Forest land = new Forest();
        harness.setLibrary(player1, List.of(instant, creature, sorcery, land));
        castClues();

        harness.handleMultipleCardsChosen(player1, List.of(instant.getId(), sorcery.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(instant, sorcery);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castClues() {
        harness.castFromHand(player1, new UncoveredClues(), "{2}{U}");
        harness.passBothPriorities();
    }

    private void setupTopFour(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }
}
