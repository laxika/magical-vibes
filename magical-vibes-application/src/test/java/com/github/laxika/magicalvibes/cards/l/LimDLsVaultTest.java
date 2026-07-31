package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LimDLsVaultTest extends BaseCardTest {

    private void castVault(List<Card> library) {
        harness.setLife(player1, 20);
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new LimDLsVault()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);
    }

    /** Eight distinguishable cards; index 0 is the top of the library. */
    private static List<Card> library8() {
        return new ArrayList<>(List.of(
                new Plains(), new Island(), new Swamp(), new Mountain(),
                new Forest(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
    }

    private void order(List<Integer> cardOrder) {
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(cardOrder));
    }

    @Test
    @DisplayName("Declining immediately costs no life and puts the five looked-at cards back on top in the chosen order")
    void declineImmediately() {
        List<Card> library = library8();
        List<Card> topFive = List.copyOf(library.subList(0, 5));
        castVault(library);

        // The five cards are held out of the library while the prompt is pending.
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);

        harness.handleMayAbilityChosen(player1, false);
        order(List.of(4, 3, 2, 1, 0));

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(8);
        assertThat(deck.subList(0, 5))
                .containsExactly(topFive.get(4), topFive.get(3), topFive.get(2), topFive.get(1), topFive.get(0));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Accepting pays 1 life, bottoms the looked-at cards in the chosen order, and looks at five more")
    void acceptPaysLifeAndLooksAgain() {
        List<Card> library = library8();
        Card sixth = library.get(5);
        Card seventh = library.get(6);
        Card eighth = library.get(7);
        castVault(library);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);

        // Bottom the first five in their original order, then five more are looked at: the three
        // cards left on top plus the first two just bottomed.
        order(List.of(0, 1, 2, 3, 4));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        harness.handleMayAbilityChosen(player1, false);
        order(List.of(0, 1, 2, 3, 4));

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(8);
        assertThat(deck.subList(0, 3)).containsExactly(sixth, seventh, eighth);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Repeating several times pays 1 life each time")
    void repeatPaysOneLifeEachTime() {
        castVault(library8());

        harness.handleMayAbilityChosen(player1, true);
        order(List.of(0, 1, 2, 3, 4));
        harness.handleMayAbilityChosen(player1, true);
        order(List.of(0, 1, 2, 3, 4));
        harness.handleMayAbilityChosen(player1, true);
        order(List.of(0, 1, 2, 3, 4));
        harness.handleMayAbilityChosen(player1, false);
        order(List.of(0, 1, 2, 3, 4));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(8);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A library smaller than five is looked at in full and comes back in the chosen order")
    void smallLibraryLooksAtEverything() {
        List<Card> library = new ArrayList<>(List.of(new Plains(), new Island(), new Swamp()));
        Card plains = library.get(0);
        Card island = library.get(1);
        Card swamp = library.get(2);
        castVault(library);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        harness.handleMayAbilityChosen(player1, false);
        order(List.of(2, 0, 1));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(swamp, plains, island);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("An empty library looks at nothing and prompts nothing")
    void emptyLibraryDoesNothing() {
        castVault(new ArrayList<>());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An order that is not a permutation of the looked-at cards is rejected")
    void rejectsInvalidOrder() {
        castVault(library8());

        harness.handleMayAbilityChosen(player1, false);

        assertThatThrownBy(() -> order(List.of(0, 0, 1, 2, 3)))
                .isInstanceOf(IllegalStateException.class);
    }
}
