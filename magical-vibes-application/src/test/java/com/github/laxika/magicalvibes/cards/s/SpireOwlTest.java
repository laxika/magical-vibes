package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SpireOwl.class)
class SpireOwlTest extends BaseCardTest {

    @Test
    void enteringBattlefieldLetsControllerReorderTopFourCards() {
        Card top0 = new SpireOwl();
        Card top1 = new SpireOwl();
        Card top2 = new SpireOwl();
        Card top3 = new SpireOwl();
        harness.setLibrary(player1, List.of(top0, top1, top2, top3));

        List<Card> deck = gd.playerDecks.get(player1.getId());

        harness.castFromHand(player1, new SpireOwl(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.cards()).containsExactly(top0, top1, top2, top3);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(deck.subList(0, 4)).containsExactly(top3, top2, top1, top0);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void enteringWithFewerThanFourCardsReordersAvailableCards() {
        Card cardA = new SpireOwl();
        Card cardB = new SpireOwl();
        harness.setLibrary(player1, List.of(cardA, cardB));
        List<Card> deck = gd.playerDecks.get(player1.getId());

        harness.castFromHand(player1, new SpireOwl(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactly(cardA, cardB);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(deck).containsExactly(cardB, cardA);
    }

    @Test
    void enteringWithOneLibraryCardLooksAtThatCardWithoutPromptingForOrder() {
        Card onlyCard = new SpireOwl();
        harness.setLibrary(player1, List.of(onlyCard));

        harness.castFromHand(player1, new SpireOwl(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gameLogContains("looks at the top card")).isTrue();
    }

    @Test
    void enteringWithEmptyLibraryDoesNotPromptForOrder() {
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new SpireOwl(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gameLogContains("library is empty")).isTrue();
    }
}
