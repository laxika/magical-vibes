package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PulseOfTheGridTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two, discards one, and returns to hand when an opponent has more cards")
    void returnsToHandWhenOpponentHasMoreCards() {
        PulseOfTheGrid pulse = new PulseOfTheGrid();
        harness.setHand(player1, List.of(pulse, new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4).contains(pulse);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(pulse);
    }

    @Test
    @DisplayName("Goes to the graveyard when no opponent has more cards after the discard")
    void goesToGraveyardWhenOpponentDoesNotHaveMoreCards() {
        PulseOfTheGrid pulse = new PulseOfTheGrid();
        harness.setHand(player1, List.of(pulse, new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3).doesNotContain(pulse);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(pulse);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
