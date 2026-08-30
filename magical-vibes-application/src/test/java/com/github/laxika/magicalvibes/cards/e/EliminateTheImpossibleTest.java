package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EliminateTheImpossible.class, GrizzlyBears.class})
class EliminateTheImpossibleTest extends BaseCardTest {

    @Test
    @DisplayName("Investigates, weakens opponent creatures, and clears their suspected designation")
    void investigatesWeakensAndUnsuspectsOpponentCreatures() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        own.setSuspected(true);
        opponent.setSuspected(true);

        castEliminateTheImpossible();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(own.getEffectivePower()).isEqualTo(2);
        assertThat(opponent.getEffectivePower()).isEqualTo(0);
        assertThat(opponent.getEffectiveToughness()).isEqualTo(2);
        assertThat(own.isSuspected()).isTrue();
        assertThat(opponent.isSuspected()).isFalse();
    }

    @Test
    @DisplayName("The power reduction wears off at end of turn")
    void powerReductionWearsOffAtEndOfTurn() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castEliminateTheImpossible();
        assertThat(opponent.getEffectivePower()).isEqualTo(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(opponent.getEffectivePower()).isEqualTo(2);
    }

    private void castEliminateTheImpossible() {
        harness.setHand(player1, List.of(new EliminateTheImpossible()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
