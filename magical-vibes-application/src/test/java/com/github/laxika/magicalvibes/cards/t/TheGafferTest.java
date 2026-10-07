package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheGaffer.class})
class TheGafferTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Draws a card at each end step after gaining at least 3 life")
    void drawsAfterGainingAtLeastThreeLife() {
        harness.addToBattlefield(player1, new TheGaffer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new TheGaffer()));
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw when fewer than 3 life was gained")
    void doesNotDrawBelowLifeThreshold() {
        harness.addToBattlefield(player1, new TheGaffer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new TheGaffer()));
        gd.lifeGainedThisTurn.put(player1.getId(), 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Checks the Gaffer's life gain on an opponent's end step")
    void drawsOnOpponentEndStep() {
        harness.addToBattlefield(player1, new TheGaffer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new TheGaffer()));
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToEndStep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawsOnlyOneCardAfterGainingMoreThanThreeLife() {
        harness.addToBattlefield(player1, new TheGaffer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new TheGaffer(), new TheGaffer()));
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 9));

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void opponentsLifeGainDoesNotTriggerDraw() {
        harness.addToBattlefield(player1, new TheGaffer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new TheGaffer()));
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void lifeGainedAfterEndStepBeginsDoesNotTriggerDraw() {
        harness.addToBattlefield(player1, new TheGaffer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new TheGaffer()));

        advanceToEndStep(player1);
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void separateLifeGainsCountEvenAfterLosingLife() {
        harness.addToBattlefield(player1, new TheGaffer());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new TheGaffer()));
        harness.inMutationScope(() -> {
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1);
            harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 5, "life loss");
            harness.getLifeSupport().applyGainLife(gd, player1.getId(), 2);
        });

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
