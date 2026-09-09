package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheTenRings.class, GrizzlyBears.class})
class TheTenRingsTest extends BaseCardTest {

    @Test
    @DisplayName("Raises its controller's maximum hand size to ten")
    void maximumHandSizeIsTen() {
        harness.addToBattlefield(player1, new TheTenRings());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, cards(11));

        gs.advanceStep(gd);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Draws exactly enough cards to reach ten at the controller's end step")
    void drawsUpToTenCardsAtEndStep() {
        harness.addToBattlefield(player1, new TheTenRings());
        harness.setHand(player1, cards(6));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        gs.advanceStep(gd);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(10);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 4);
    }

    @Test
    @DisplayName("Does not draw when the controller already has ten cards")
    void doesNotDrawAtTenCards() {
        harness.addToBattlefield(player1, new TheTenRings());
        harness.setHand(player1, cards(10));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);

        assertThat(gd.stack).isEmpty();
    }

    private List<com.github.laxika.magicalvibes.model.Card> cards(int count) {
        List<com.github.laxika.magicalvibes.model.Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
