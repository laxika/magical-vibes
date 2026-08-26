package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodtitheCollector.class, GrizzlyBears.class})
class BloodtitheCollectorTest extends BaseCardTest {

    @Test
    @DisplayName("Does nothing when no opponent lost life this turn")
    void noTriggerWithoutOpponentLifeLoss() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        castBloodtitheCollector();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each opponent discards a card after an opponent lost life")
    void eachOpponentDiscardsAfterOpponentLifeLoss() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));

        castBloodtitheCollector();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does nothing when only you lost life this turn")
    void noTriggerFromControllerLifeLoss() {
        gd.lifeLostThisTurn.put(player1.getId(), 1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        castBloodtitheCollector();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does nothing when the opponent has no cards in hand")
    void noDiscardWithEmptyOpponentHand() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setHand(player2, new ArrayList<>());

        castBloodtitheCollector();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castBloodtitheCollector() {
        harness.setHand(player1, List.of(new BloodtitheCollector()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
