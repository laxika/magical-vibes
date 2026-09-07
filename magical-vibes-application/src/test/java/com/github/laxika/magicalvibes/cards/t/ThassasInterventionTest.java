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
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(first, second, third, untouched));

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
}
