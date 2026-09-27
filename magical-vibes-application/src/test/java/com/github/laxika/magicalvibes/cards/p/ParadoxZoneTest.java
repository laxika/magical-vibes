package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ParadoxZone.class)
class ParadoxZoneTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles growth counters and creates a Fractal with that many +1/+1 counters")
    void doublesGrowthCountersBeforeCreatingFractal() {
        Permanent zone = harness.enterBattlefieldAndReturn(player1, new ParadoxZone());

        assertThat(zone.getCounterCount(CounterType.GROWTH)).isEqualTo(1);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(zone.getCounterCount(CounterType.GROWTH)).isEqualTo(2);
        Permanent fractal = findPermanent(player1, "Fractal");
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(fractal.getEffectivePower()).isEqualTo(2);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Triggers only during its controller's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        Permanent zone = harness.enterBattlefieldAndReturn(player1, new ParadoxZone());

        advanceToEndStep(player2);

        assertThat(zone.getCounterCount(CounterType.GROWTH)).isEqualTo(1);
        assertThat(findPermanents(player1, "Fractal")).isEmpty();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
