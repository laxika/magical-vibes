package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CabalCoffers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Breakthrough.class, CabalCoffers.class})
class BreakthroughTest extends BaseCardTest {

    @Test
    @DisplayName("Draws four cards, then discards down to X cards")
    void drawsFourThenDiscardsDownToX() {
        harness.setLibrary(player1, List.of(
                new CabalCoffers(), new CabalCoffers(), new CabalCoffers(), new CabalCoffers()));
        harness.setHand(player1, List.of(new Breakthrough(), new CabalCoffers(), new CabalCoffers()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("X zero discards the whole hand after drawing")
    void xZeroDiscardsWholeHand() {
        harness.setLibrary(player1, List.of(
                new CabalCoffers(), new CabalCoffers(), new CabalCoffers(), new CabalCoffers()));
        harness.setHand(player1, List.of(new Breakthrough(), new CabalCoffers(), new CabalCoffers()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        for (int i = 0; i < 6; i++) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Does not discard when X exceeds the post-draw hand size")
    void xExceedsPostDrawHandSize() {
        harness.setLibrary(player1, List.of(
                new CabalCoffers(), new CabalCoffers(), new CabalCoffers(), new CabalCoffers()));
        harness.setHand(player1, List.of(new Breakthrough()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, 5);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }
}
