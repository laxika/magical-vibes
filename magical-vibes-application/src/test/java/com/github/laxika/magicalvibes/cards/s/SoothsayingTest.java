package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SoothsayingTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability shuffles the controller's library")
    void shufflesLibrary() {
        harness.addToBattlefield(player1, new Soothsaying());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        List<Card> before = List.copyOf(deck);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(deck).containsExactlyInAnyOrderElementsOf(before);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("shuffles their library"));
    }

    @Test
    @DisplayName("The X ability reorders the top X cards")
    void reordersTopXCards() {
        harness.addToBattlefield(player1, new Soothsaying());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = deck.get(0);
        Card originalSecond = deck.get(1);
        Card originalThird = deck.get(2);

        harness.activateAbility(player1, 0, 1, 3, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(originalTop, originalSecond, originalThird);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(deck.subList(0, 3)).containsExactly(originalThird, originalTop, originalSecond);
    }
}
