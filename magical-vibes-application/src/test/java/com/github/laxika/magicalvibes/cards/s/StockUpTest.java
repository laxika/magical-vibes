package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StockUpTest extends BaseCardTest {

    @Test
    @DisplayName("Puts two chosen cards into hand and orders the rest on the bottom")
    void choosesTwoCardsAndOrdersTheRest() {
        Card top1 = new GrizzlyBears();
        Card top2 = new LlanowarElves();
        Card top3 = new Island();
        Card top4 = new Plains();
        Card top5 = new GrizzlyBears();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(top1, top2, top3, top4, top5));

        harness.setHand(player1, List.of(new StockUp()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

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
}
