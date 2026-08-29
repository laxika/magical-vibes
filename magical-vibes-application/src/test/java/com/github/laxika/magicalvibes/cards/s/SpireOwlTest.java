package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpireOwlTest extends BaseCardTest {

    @Test
    void enteringBattlefieldLetsControllerReorderTopFourCards() {
        harness.setHand(player1, List.of(new SpireOwl()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);
        Card top2 = deck.get(2);
        Card top3 = deck.get(3);

        harness.castCreature(player1, 0);
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
        harness.setHand(player1, List.of(new SpireOwl()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        Card cardA = new SpireOwl();
        Card cardB = new SpireOwl();
        deck.add(cardA);
        deck.add(cardB);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactly(cardA, cardB);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(deck).containsExactly(cardB, cardA);
    }
}
