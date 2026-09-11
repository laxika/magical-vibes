package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Concentrate;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FractalTenderTest extends BaseCardTest {

    @Test
    @DisplayName("Increment creates a 3/3 Fractal with three +1/+1 counters at the end step")
    void createsFractalAfterCounterWasPutOnIt() {
        Permanent tender = harness.addToBattlefieldAndReturn(player1, new FractalTender());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Concentrate()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(tender.getPlusOnePlusOneCounters()).isEqualTo(1);

        advanceToEndStepAndResolve(player1);

        List<Permanent> fractals = findPermanents(player1, "Fractal");
        assertThat(fractals).hasSize(1);
        Permanent fractal = fractals.getFirst();
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(fractal.getEffectivePower()).isEqualTo(3);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not create a Fractal when no counter was put on it this turn")
    void doesNotCreateFractalWithoutCounterPlacement() {
        harness.addToBattlefield(player1, new FractalTender());

        advanceToEndStepAndResolve(player1);

        assertThat(findPermanents(player1, "Fractal")).isEmpty();
    }

    private void advanceToEndStepAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
