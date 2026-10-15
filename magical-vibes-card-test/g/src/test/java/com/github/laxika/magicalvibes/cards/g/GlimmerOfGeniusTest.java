package com.github.laxika.magicalvibes.cards.g;

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

@CardUsed({GlimmerOfGenius.class})
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
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(topCard, secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.assertInGraveyard(player1, "Glimmer of Genius");
    }

    @Test
    @DisplayName("Bottoming both scry cards draws the next two and adds to existing energy")
    void bottomsBothBeforeDrawingAndAddsEnergy() {
        Card first = new GlimmerOfGenius();
        Card second = new GlimmerOfGenius();
        Card third = new GlimmerOfGenius();
        Card fourth = new GlimmerOfGenius();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new GlimmerOfGenius()));
        gd.playerEnergyCounters.put(player1.getId(), 3);
        gd.playerEnergyCounters.put(player2.getId(), 5);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third, fourth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(5);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(5);
        harness.assertInGraveyard(player1, "Glimmer of Genius");
    }

    @Test
    @DisplayName("Keeping one scry card and bottoming the other determines the two draws")
    void keepsOneAndBottomsOneBeforeDrawing() {
        Card first = new GlimmerOfGenius();
        Card second = new GlimmerOfGenius();
        Card third = new GlimmerOfGenius();
        Card fourth = new GlimmerOfGenius();
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        harness.setHand(player1, List.of(new GlimmerOfGenius()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth, first);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.assertInGraveyard(player1, "Glimmer of Genius");
    }

    @Test
    @DisplayName("A two-card library can be reordered before both cards are drawn")
    void reordersBothCardsInTwoCardLibrary() {
        Card first = new GlimmerOfGenius();
        Card second = new GlimmerOfGenius();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new GlimmerOfGenius()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveInstant(player1, 0);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.assertInGraveyard(player1, "Glimmer of Genius");
    }
}
