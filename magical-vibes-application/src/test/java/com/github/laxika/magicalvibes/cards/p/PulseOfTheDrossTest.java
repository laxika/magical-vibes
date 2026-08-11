package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PulseOfTheDrossTest extends BaseCardTest {

    @Test
    @DisplayName("The chosen revealed card is discarded and the spell returns when the target has more cards")
    void discardsChosenCardAndReturnsWhenTargetHasMoreCards() {
        PulseOfTheDross pulse = new PulseOfTheDross();
        Island island = new Island();
        GrizzlyBears bears = new GrizzlyBears();
        Peek peek = new Peek();
        HornedTurtle turtle = new HornedTurtle();
        harness.setHand(player1, List.of(pulse, island));
        harness.setHand(player2, List.of(bears, peek, turtle));
        addMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealCardsDiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island, pulse);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(peek);
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrder(bears, turtle);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(pulse);
    }

    @Test
    @DisplayName("The spell goes to the graveyard when the target does not have more cards after discarding")
    void goesToGraveyardWhenTargetDoesNotHaveMoreCards() {
        PulseOfTheDross pulse = new PulseOfTheDross();
        Island island = new Island();
        GrizzlyBears bears = new GrizzlyBears();
        HornedTurtle turtle = new HornedTurtle();
        harness.setHand(player1, List.of(pulse, island));
        harness.setHand(player2, List.of(bears, turtle));
        addMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(pulse);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
