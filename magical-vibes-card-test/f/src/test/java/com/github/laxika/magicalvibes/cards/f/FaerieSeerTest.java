package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaerieSeer.class})
class FaerieSeerTest extends BaseCardTest {

    @Test
    void enteringBattlefieldScriesTwo() {
        Card first = new FaerieSeer();
        Card second = new FaerieSeer();
        Card third = new FaerieSeer();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new FaerieSeer()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(second, third, first);
    }

    @Test
    void canKeepBothCardsOnTopInEitherOrder() {
        Card first = new FaerieSeer();
        Card second = new FaerieSeer();
        Card third = new FaerieSeer();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new FaerieSeer()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(second, first, third);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canPutBothCardsOnBottomInEitherOrder() {
        Card first = new FaerieSeer();
        Card second = new FaerieSeer();
        Card third = new FaerieSeer();
        Card opponentCard = new FaerieSeer();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new FaerieSeer()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(third, second, first);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void scriesOnlyAvailableCardInOneCardLibrary() {
        Card onlyCard = new FaerieSeer();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new FaerieSeer()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyLibraryDoesNotRequireScryInput() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new FaerieSeer()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Faerie Seer")).isEqualTo(1);
    }
}
