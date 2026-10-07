package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TemurTawnyback.class, Forest.class})
class TemurTawnybackTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws a card, then prompts its controller to discard a card")
    void etbDrawsThenDiscards() {
        harness.setHand(player1, List.of(new TemurTawnyback(), new TemurTawnyback()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Temur Tawnyback");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller can discard the card just drawn")
    void canDiscardNewlyDrawnCard() {
        harness.setHand(player1, List.of(new TemurTawnyback(), new TemurTawnyback()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 1);

        harness.assertInHand(player1, "Temur Tawnyback");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty hand after casting still draws and discards")
    void drawsAndDiscardsWithNoOtherCardsInHand() {
        harness.setHand(player1, List.of(new TemurTawnyback()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertOnBattlefield(player1, "Temur Tawnyback");
        assertThat(gd.stack).isEmpty();
    }
}
