package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThassasIntervention.class, GrizzlyBears.class, LlanowarElves.class})
class ThassasInterventionTest extends BaseCardTest {

    @Test
    @DisplayName("Looks at X cards, puts up to two into hand, and bottoms the rest randomly")
    void looksAtXCardsAndKeepsTwo() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        Card third = new GrizzlyBears();
        Card untouched = new LlanowarElves();
        harness.setLibrary(player1, List.of(first, second, third, untouched));
        List<Card> deck = gd.playerDecks.get(player1.getId());

        harness.setHand(player1, List.of(new ThassasIntervention()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castModalInstantForX(player1, 0, 0, 3, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.allCards()).containsExactly(first, second, third);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.randomRemainingToBottom()).isTrue();
        assertThat(choice.reorderRemainingToBottom()).isFalse();

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(first, second)
                .doesNotContain(third, untouched);
        assertThat(deck).containsExactly(untouched, third);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The counter mode requires twice X mana")
    void counterModeRequiresTwiceX() {
        harness.forceActivePlayer(player2);
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player2, List.of(elves));
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.setHand(player1, List.of(new ThassasIntervention()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castModalInstantForX(player1, 0, 1, 2, elves.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void mayKeepFewerThanTwoCards(int keepCount) {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        Card third = new GrizzlyBears();
        Card untouched = new LlanowarElves();
        harness.setLibrary(player1, List.of(first, second, third, untouched));
        harness.setHand(player1, List.of(new ThassasIntervention()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castModalInstantForX(player1, 0, 0, 3, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1,
                keepCount == 0 ? List.of() : List.of(first.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(keepCount);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck.getFirst()).isSameAs(untouched);
        assertThat(deck.subList(1, deck.size())).containsExactlyInAnyOrderElementsOf(
                keepCount == 0 ? List.of(first, second, third) : List.of(second, third));
        if (keepCount == 1) {
            assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        }
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void mayKeepNoCardsWhenLibraryHasAtMostTwo(int librarySize) {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        List<Card> cards = librarySize == 1 ? List.of(first) : List.of(first, second);
        harness.setLibrary(player1, cards);
        harness.setHand(player1, List.of(new ThassasIntervention()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castModalInstantForX(player1, 0, 0, 3, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(cards);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void zeroXLeavesLibraryUnchanged() {
        Card first = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first));
        harness.setHand(player1, List.of(new ThassasIntervention()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castModalInstantForX(player1, 0, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Thassa's Intervention");
    }

    @ParameterizedTest
    @CsvSource({"2, true", "2, false", "0, true", "0, false"})
    void spellControllerMayPayOrDeclineTwiceX(int x, boolean pay) {
        harness.forceActivePlayer(player2);
        LlanowarElves elves = new LlanowarElves();
        harness.setHand(player2, List.of(elves));
        harness.addMana(player2, ManaColor.GREEN, 1 + 2 * x);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.setHand(player1, List.of(new ThassasIntervention()));
        harness.addMana(player1, ManaColor.BLUE, x + 2);

        harness.castModalInstantForX(player1, 0, 1, x, elves.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player2, pay);

        if (pay) {
            harness.passBothPriorities();
            harness.assertOnBattlefield(player2, "Llanowar Elves");
            harness.assertNotInGraveyard(player2, "Llanowar Elves");
        } else {
            harness.assertInGraveyard(player2, "Llanowar Elves");
            harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        }
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
