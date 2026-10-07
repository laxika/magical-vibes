package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StockUp.class, Island.class, Plains.class})
class StockUpTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two chosen cards into hand and orders the rest on the bottom")
    void choosesTwoCardsAndOrdersTheRest() {
        Card top1 = new Island();
        Card top2 = new Plains();
        Card top3 = new Island();
        Card top4 = new Plains();
        Card top5 = new Island();
        harness.setLibrary(player1, List.of(top1, top2, top3, top4, top5));

        harness.setHand(player1, List.of(new StockUp()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.reorderRemainingToBottom()).isTrue();
        assertThat(choice.randomRemainingToBottom()).isFalse();
        assertThat(choice.allCards()).containsExactly(top1, top2, top3, top4, top5);

        harness.handleMultipleCardsChosen(player1, List.of(top2.getId(), top4.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerHands.get(player1.getId())).containsExactly(top2, top4);
        assertThat(gameData.playerDecks.get(player1.getId()))
                .containsExactly(top1, top3, top5);
        assertThat(gameData.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void putsUnchosenCardsBelowUntouchedLibraryInChosenOrder() {
        Card first = new Island();
        Card second = new Plains();
        Card third = new Island();
        Card fourth = new Plains();
        Card fifth = new Island();
        Card untouched = new Plains();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth, untouched));
        harness.setHand(player1, List.of(new StockUp()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(second.getId(), fourth.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second, fourth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, fifth, first, third);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void putsAllAvailableCardsIntoHandWhenLibraryHasAtMostTwo(int count) {
        List<Card> cards = List.<Card>of(new Island(), new Plains()).subList(0, count);
        harness.setLibrary(player1, cards);
        harness.setHand(player1, List.of(new StockUp()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(cards);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Stock Up");
    }

    @Test
    void requiresTwoCardsAndHandlesThreeCardLibrary() {
        Card first = new Island();
        Card second = new Plains();
        Card third = new Island();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new StockUp()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), third.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
