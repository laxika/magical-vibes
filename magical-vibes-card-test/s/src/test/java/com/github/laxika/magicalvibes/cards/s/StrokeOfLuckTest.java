package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StrokeOfLuck.class, GrizzlyBears.class, Shock.class})
class StrokeOfLuckTest extends BaseCardTest {

    @Test
    void choosesANameAndPutsAllMatchingLookedAtCardsIntoHand() {
        Card firstBears = new GrizzlyBears();
        Card shock = new Shock();
        Card secondBears = new GrizzlyBears();
        Card secondShock = new Shock();
        Card untouched = new Shock();
        harness.setLibrary(player1, List.of(firstBears, shock, secondBears, secondShock, untouched));
        harness.setHand(player1, List.of(new StrokeOfLuck()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(firstBears, shock, secondBears, secondShock);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstBears, secondBears);
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(shock, secondShock, untouched);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void losesOneLifeWhenTheChosenNameAppearsOnce() {
        Card bears = new GrizzlyBears();
        Card firstShock = new Shock();
        Card secondShock = new Shock();
        Card thirdShock = new Shock();
        harness.setLibrary(player1, List.of(bears, firstShock, secondShock, thirdShock));
        harness.setHand(player1, List.of(new StrokeOfLuck()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                firstShock, secondShock, thirdShock);
    }

    @Test
    void emptyLibraryDoesNotLoseLifeOrRequireAChoice() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new StrokeOfLuck()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Stroke of Luck");
    }

    @Test
    void singleCardLibraryPutsTheOnlyCardIntoHandAndLosesOneLife() {
        Card onlyCard = new StrokeOfLuck();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new StrokeOfLuck()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 19);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void takesAllFourMatchingCardsButLeavesTheFifthMatchingCardInTheLibrary() {
        Card first = new StrokeOfLuck();
        Card second = new StrokeOfLuck();
        Card third = new StrokeOfLuck();
        Card fourth = new StrokeOfLuck();
        Card fifth = new StrokeOfLuck();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth));
        harness.setHand(player1, List.of(new StrokeOfLuck()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 2);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second, third, fourth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifth);
        harness.assertLife(player1, 16);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void shortLibraryOffersAllRemainingCardsAndBottomsTheUnchosenCard() {
        Card bears = new GrizzlyBears();
        Card firstShock = new Shock();
        Card secondShock = new Shock();
        harness.setLibrary(player1, List.of(bears, firstShock, secondShock));
        harness.setHand(player1, List.of(new StrokeOfLuck()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(bears, firstShock, secondShock);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstShock, secondShock);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        harness.assertLife(player1, 18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void unchosenCardsGoBelowTheUntouchedLibraryWithoutReorderingIt() {
        Card firstBears = new GrizzlyBears();
        Card firstShock = new Shock();
        Card secondBears = new GrizzlyBears();
        Card secondShock = new Shock();
        Card untouchedFirst = new StrokeOfLuck();
        Card untouchedSecond = new GrizzlyBears();
        harness.setLibrary(player1,
                List.of(firstBears, firstShock, secondBears, secondShock, untouchedFirst, untouchedSecond));
        harness.setHand(player1, List.of(new StrokeOfLuck()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 3);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstShock, secondShock);
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(4);
        assertThat(library.subList(0, 2)).containsExactly(untouchedFirst, untouchedSecond);
        assertThat(library.subList(2, 4)).containsExactlyInAnyOrder(firstBears, secondBears);
        harness.assertLife(player1, 18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
