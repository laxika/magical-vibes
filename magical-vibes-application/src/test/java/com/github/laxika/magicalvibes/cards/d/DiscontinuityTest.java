package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiscontinuityTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for {1}{U} during your turn and ends the turn")
    void reducedCostEndsTurn() {
        harness.setHand(player1, List.of(new Discontinuity()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("The turn ends.")).isTrue();
    }

    @Test
    @DisplayName("Costs its full amount outside your turn")
    void reductionOnlyAppliesDuringYourTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Discontinuity()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
