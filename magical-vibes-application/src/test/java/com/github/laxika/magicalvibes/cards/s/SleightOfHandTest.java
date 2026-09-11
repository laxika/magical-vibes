package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SleightOfHand.class, GrizzlyBears.class, LlanowarElves.class})
class SleightOfHandTest extends BaseCardTest {

    @Test
    @DisplayName("Chosen card goes to hand; the other goes to the bottom of the library")
    void chosenCardToHandOtherToBottom() {
        Card top1 = new GrizzlyBears();
        Card top2 = new LlanowarElves();
        harness.setLibrary(player1, List.of(top1, top2));
        harness.castFromHand(player1, new SleightOfHand(), "{U}");

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(top1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top1);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(top2);
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(top2);
        harness.assertInGraveyard(player1, "Sleight of Hand");
    }

    @Test
    @DisplayName("Can keep the second card instead; the first goes to the bottom")
    void canKeepSecondCard() {
        Card top1 = new GrizzlyBears();
        Card top2 = new LlanowarElves();
        harness.setLibrary(player1, List.of(top1, top2));
        harness.castFromHand(player1, new SleightOfHand(), "{U}");

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(top2.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top2);
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(top1);
    }

    @Test
    @DisplayName("Looking at cards does not publicly reveal the chosen card")
    void chosenCardIsNotPubliclyRevealed() {
        Card chosenCard = new GrizzlyBears();
        Card otherCard = new LlanowarElves();
        harness.setLibrary(player1, List.of(chosenCard, otherCard));
        harness.castFromHand(player1, new SleightOfHand(), "{U}");

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosenCard.getId()));

        assertThat(gameLogContains(chosenCard.getName())).isFalse();
    }

    @Test
    @DisplayName("With only one card in the library, that card goes to hand")
    void oneCardLibraryPutsThatCardIntoHand() {
        Card onlyCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.castFromHand(player1, new SleightOfHand(), "{U}");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Sleight of Hand");
    }

    @Test
    @DisplayName("With an empty library, the spell resolves without a card choice")
    void emptyLibraryResolvesWithoutChoice() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new SleightOfHand(), "{U}");

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Sleight of Hand");
    }
}
