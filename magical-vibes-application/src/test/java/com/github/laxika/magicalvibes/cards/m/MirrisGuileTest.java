package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MirrisGuileTest extends BaseCardTest {

    private void advanceToUpkeepTrigger() {
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
        harness.passBothPriorities(); // resolve triggered ability → MayEffect prompts
    }

    @Test
    @DisplayName("Accepting the upkeep trigger offers the top three cards for reorder")
    void acceptingOffersTopThreeForReorder() {
        harness.addToBattlefield(player1, new MirrisGuile());
        harness.forceActivePlayer(player1);

        advanceToUpkeepTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).playerId())
                .isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(3);
    }

    @Test
    @DisplayName("Chosen order is applied to the top of the library")
    void chosenOrderIsAppliedToLibrary() {
        harness.addToBattlefield(player1, new MirrisGuile());
        harness.forceActivePlayer(player1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);
        Card top2 = deck.get(2);

        advanceToUpkeepTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 1, 0)));

        assertThat(deck.get(0)).isSameAs(top2);
        assertThat(deck.get(1)).isSameAs(top1);
        assertThat(deck.get(2)).isSameAs(top0);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the upkeep trigger leaves the library untouched")
    void decliningLeavesLibraryUntouched() {
        harness.addToBattlefield(player1, new MirrisGuile());
        harness.forceActivePlayer(player1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);

        advanceToUpkeepTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(deck.get(0)).isSameAs(top0);
    }

    @Test
    @DisplayName("Does not trigger on the opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new MirrisGuile());
        harness.forceActivePlayer(player2);

        advanceToUpkeepTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library with fewer than three cards only offers what is there")
    void shortLibraryOffersFewerCards() {
        harness.addToBattlefield(player1, new MirrisGuile());
        harness.forceActivePlayer(player1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card cardA = deck.get(0);
        Card cardB = deck.get(1);
        deck.clear();
        deck.add(cardA);
        deck.add(cardB);

        advanceToUpkeepTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(2);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(deck.get(0)).isSameAs(cardB);
        assertThat(deck.get(1)).isSameAs(cardA);
    }
}
