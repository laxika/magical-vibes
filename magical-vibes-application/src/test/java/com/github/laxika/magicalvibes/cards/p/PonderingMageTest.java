package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PonderingMage.class)
class PonderingMageTest extends BaseCardTest {

    @Test
    void enteringLetsItsControllerReorderTheTopThreeCards() {
        harness.setHand(player1, List.of(new PonderingMage()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);
        Card top2 = deck.get(2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(top0, top1, top2);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 1, 0)));

        assertThat(deck).containsSubsequence(top2, top1, top0);
    }

    @Test
    void decliningShuffleDrawsTheReorderedTopCard() {
        harness.setHand(player1, List.of(new PonderingMage()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top2 = deck.get(2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(top2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void acceptingShuffleStillDrawsOneCard() {
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new PonderingMage()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
