package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.testutil.TestStackDoor;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YourNightmaresAreDelicious.class, Forest.class, GrizzlyBears.class})
class YourNightmaresAreDeliciousTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent discards down to five, and fewer than three discards lets you draw three")
    void drawsThreeWhenFewerThanThreeCardsAreDiscarded() {
        List<Card> opponentHand = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears());
        List<Card> drawn = List.of(new Forest(), new Forest(), new Forest());
        harness.setHand(player1, List.of());
        harness.setHand(player2, opponentHand);
        harness.setLibrary(player1, drawn);

        resolveScheme();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class))
                .isNotNull();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(5);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(drawn);
    }

    @Test
    @DisplayName("Three or more cards discarded this way means you draw nothing")
    void doesNotDrawWhenAtLeastThreeCardsAreDiscarded() {
        List<Card> opponentHand = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        Card libraryCard = new Forest();
        harness.setHand(player2, opponentHand);
        harness.setLibrary(player1, List.of(libraryCard));

        resolveScheme();

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 1);
        harness.handleCardChosen(player2, 2);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(5);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(libraryCard);
    }

    @Test
    @DisplayName("No opponent above five cards discards nothing, and you draw three")
    void drawsThreeWhenNoOpponentHasMoreThanFiveCards() {
        List<Card> drawn = List.of(new Forest(), new Forest(), new Forest());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, drawn);

        resolveScheme();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(drawn);
    }

    private void resolveScheme() {
        YourNightmaresAreDelicious scheme = new YourNightmaresAreDelicious();
        gd.stack.add(TestStackDoor.triggered(scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getCastingCostsAndSpellEffects()));
        harness.passBothPriorities();
        resolveAllTriggers();
    }
}
