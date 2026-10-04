package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlowState.class, GrizzlyBears.class, HillGiant.class, Shock.class, Divination.class})
class FlowStateTest extends BaseCardTest {

    @Test
    @DisplayName("Keeps one card without both an instant and a sorcery in the graveyard")
    void keepsOneWithoutInstantAndSorcery() {
        Card first = new GrizzlyBears();
        Card second = new HillGiant();
        Card third = new GrizzlyBears();
        setTopThree(first, second, third);

        castFlowState();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        finishBottomOrder(List.of(0, 1));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third);
    }

    @Test
    @DisplayName("Keeps two cards when the graveyard has an instant and a sorcery")
    void keepsTwoWithInstantAndSorcery() {
        harness.setGraveyard(player1, List.of(new Shock(), new Divination()));
        Card first = new GrizzlyBears();
        Card second = new HillGiant();
        Card third = new GrizzlyBears();
        setTopThree(first, second, third);

        castFlowState();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }

    @Test
    @DisplayName("Two instants without a sorcery do not unlock the second card")
    void needsBothCardTypes() {
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));
        Card first = new GrizzlyBears();
        Card second = new HillGiant();
        Card third = new GrizzlyBears();
        setTopThree(first, second, third);

        castFlowState();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        finishBottomOrder(List.of(0, 1));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third);
    }

    @Test
    void resolvingSpellDoesNotCountAsASorceryInTheGraveyard() {
        harness.setGraveyard(player1, List.of(new Shock()));
        Card first = new FlowState();
        Card second = new FlowState();
        Card third = new FlowState();
        setTopThree(first, second, third);

        castFlowState();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        finishBottomOrder(List.of(1, 0));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, first);
    }

    @Test
    void opponentsGraveyardDoesNotUnlockTheSecondCard() {
        harness.setGraveyard(player2, List.of(new Shock(), new Divination()));
        Card first = new FlowState();
        Card second = new FlowState();
        Card third = new FlowState();
        setTopThree(first, second, third);

        castFlowState();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        finishBottomOrder(List.of(0, 1));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third);
    }

    @Test
    void putsRemainingCardsBelowUntouchedCardsInChosenOrder() {
        Card first = new FlowState();
        Card second = new FlowState();
        Card third = new FlowState();
        Card fourth = new FlowState();
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        castFlowState();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        finishBottomOrder(List.of(1, 0));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth, third, first);
    }

    @Test
    void takesBothCardsWhenEnhancedWithOnlyTwoCardsInLibrary() {
        harness.setGraveyard(player1, List.of(new Shock(), new FlowState()));
        Card first = new FlowState();
        Card second = new FlowState();
        harness.setLibrary(player1, List.of(first, second));

        castFlowState();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void takesTheOnlyCardInLibrary() {
        Card only = new FlowState();
        harness.setLibrary(player1, List.of(only));

        castFlowState();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(only);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryRequiresNoChoice() {
        harness.setLibrary(player1, List.of());

        castFlowState();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castFlowState() {
        harness.castFromHand(player1, new FlowState(), "{1}{U}");
        harness.passBothPriorities();
    }

    private void setTopThree(Card first, Card second, Card third) {
        harness.setLibrary(player1, List.of(first, second, third));
    }

    private void finishBottomOrder(List<Integer> order) {
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.CardOrder(order));
    }
}
