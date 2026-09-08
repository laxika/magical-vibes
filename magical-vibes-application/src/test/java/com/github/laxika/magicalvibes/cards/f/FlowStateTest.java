package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private void castFlowState() {
        harness.setHand(player1, List.of(new FlowState()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void setTopThree(Card first, Card second, Card third) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(first, second, third));
    }

    private void finishBottomOrder(List<Integer> order) {
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.CardOrder(order));
    }
}
