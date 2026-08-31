package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AvenFateshaper.class)
class AvenFateshaperTest extends BaseCardTest {

    @Test
    @DisplayName("Entering lets its controller reorder the top four cards")
    void enteringReordersTopFourCards() {
        harness.setHand(player1, List.of(new AvenFateshaper()));
        harness.addMana(player1, ManaColor.BLUE, 7);

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
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(top0, top1, top2, top3);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(deck).containsSubsequence(top3, top2, top1, top0);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Activating the ability lets its controller reorder the top four cards")
    void activatedAbilityReordersTopFourCards() {
        addCreatureReady(player1, new AvenFateshaper());
        harness.addMana(player1, ManaColor.BLUE, 5);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);
        Card top2 = deck.get(2);
        Card top3 = deck.get(3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(top0, top1, top2, top3);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 3, 0, 2)));

        assertThat(deck).containsSubsequence(top1, top3, top0, top2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
