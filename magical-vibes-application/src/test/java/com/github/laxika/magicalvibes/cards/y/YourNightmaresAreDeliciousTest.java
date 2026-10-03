package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YourNightmaresAreDelicious.class, Forest.class, GrizzlyBears.class})
class YourNightmaresAreDeliciousTest extends BaseCardTest {

    @Test
    void drawsThreeWhenFewerThanThreeCardsAreDiscarded() {
        List<Card> opponentHand = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears());
        List<Card> drawn = List.of(new Forest(), new Forest(), new Forest());
        harness.setHand(player2, opponentHand);
        harness.setLibrary(player1, drawn);

        resolveScheme();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class))
                .isNotNull();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(5);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(drawn);
    }

    @Test
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
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(5);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(libraryCard);
    }

    @Test
    void drawsThreeWhenNoOpponentHasMoreThanFiveCards() {
        List<Card> drawn = List.of(new Forest(), new Forest(), new Forest());
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, drawn);

        resolveScheme();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(drawn);
    }

    private void resolveScheme() {
        YourNightmaresAreDelicious scheme = new YourNightmaresAreDelicious();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL)));
        harness.passBothPriorities();
    }
}
