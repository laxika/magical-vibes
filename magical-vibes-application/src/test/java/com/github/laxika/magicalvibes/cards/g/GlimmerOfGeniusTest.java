package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GlimmerOfGeniusTest extends BaseCardTest {

    @Test
    @DisplayName("Glimmer of Genius scries two before drawing two and granting two energy")
    void scriesDrawsAndGrantsEnergy() {
        gd.playerEnergyCounters.put(player1.getId(), 0);
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card topCard = deck.get(0);
        Card secondCard = deck.get(1);
        int deckSizeBefore = deck.size();

        harness.setHand(player1, List.of(new GlimmerOfGenius()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard, secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.assertInGraveyard(player1, "Glimmer of Genius");
    }
}
