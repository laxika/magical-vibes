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

@CardUsed({ParadoxZone.class})
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

    @Test
    @DisplayName("Successive end steps create separate Fractals without growing earlier tokens")
    void successiveEndStepsDoubleAgain() {
        Permanent zone = harness.enterBattlefieldAndReturn(player1, new ParadoxZone());
        advanceToEndStep(player1);
        harness.passBothPriorities();
        Permanent firstFractal = findPermanent(player1, "Fractal");

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(zone.getCounterCount(CounterType.GROWTH)).isEqualTo(4);
        assertThat(findPermanents(player1, "Fractal")).hasSize(2)
                .extracting(token -> token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .containsExactlyInAnyOrder(2, 4);
        assertThat(firstFractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Uses the growth counter count at resolution rather than at trigger time")
    void usesCurrentCounterCountAtResolution() {
        Permanent zone = harness.enterBattlefieldAndReturn(player1, new ParadoxZone());
        advanceToEndStep(player1);
        zone.setCounterCount(CounterType.GROWTH, 3);

        harness.passBothPriorities();

        assertThat(zone.getCounterCount(CounterType.GROWTH)).isEqualTo(6);
        assertThat(findPermanent(player1, "Fractal").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(6);
    }

    @Test
    @DisplayName("A removed source still creates a Fractal using its last known growth counters")
    void usesLastKnownCountersWhenSourceLeaves() {
        Permanent zone = harness.enterBattlefieldAndReturn(player1, new ParadoxZone());
        advanceToEndStep(player1);
        zone.setCounterCount(CounterType.GROWTH, 3);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, zone));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Paradox Zone");
        assertThat(findPermanent(player1, "Fractal").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(3);
    }

    @Test
    @DisplayName("With zero growth counters the 0/0 Fractal does not survive resolution")
    void zeroGrowthCountersDoNotProduceSurvivingFractal() {
        Permanent zone = harness.enterBattlefieldAndReturn(player1, new ParadoxZone());
        zone.setCounterCount(CounterType.GROWTH, 0);
        advanceToEndStep(player1);

        harness.passBothPriorities();

        assertThat(zone.getCounterCount(CounterType.GROWTH)).isZero();
        assertThat(findPermanents(player1, "Fractal")).isEmpty();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
