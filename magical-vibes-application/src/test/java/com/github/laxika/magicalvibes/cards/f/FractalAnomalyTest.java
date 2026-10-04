package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FractalAnomaly.class, DoublingSeason.class})
class FractalAnomalyTest extends BaseCardTest {

    @Test
    @DisplayName("Counts actual draws made before resolution and does not resize the token afterward")
    void countsDrawsAtResolution() {
        harness.setLibrary(player1, List.of(new FractalAnomaly(), new FractalAnomaly(), new FractalAnomaly()));
        harness.getDrawService().resolveDrawCard(gd, player1.getId());
        harness.castFromHand(player1, new FractalAnomaly(), "{U}");
        harness.getDrawService().resolveDrawCard(gd, player1.getId());
        harness.passBothPriorities();

        Permanent fractal = findPermanent(player1, "Fractal");
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.getDrawService().resolveDrawCard(gd, player1.getId());
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creates a Fractal with +1/+1 counters equal to cards drawn this turn")
    void createsFractalWithCounters() {
        gd.cardsDrawnThisTurn.put(player1.getId(), 3);

        harness.castFromHand(player1, new FractalAnomaly(), "{U}");
        harness.passBothPriorities();

        Permanent fractal = findPermanent(player1, "Fractal");
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(fractal.getEffectivePower()).isEqualTo(3);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("With no cards drawn, the 0/0 Fractal dies to state-based actions")
    void zeroCountersFractalDies() {
        gd.cardsDrawnThisTurn.put(player1.getId(), 0);

        harness.castFromHand(player1, new FractalAnomaly(), "{U}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fractal");
    }

    @Test
    @DisplayName("Uses the spell controller's draws even on an opponent's turn")
    void usesControllersDrawCount() {
        gd.cardsDrawnThisTurn.put(player1.getId(), 5);
        gd.cardsDrawnThisTurn.put(player2.getId(), 1);

        harness.castFromHand(player2, new FractalAnomaly(), "{U}");
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Fractal").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Fractal");
    }

    @Test
    @DisplayName("Puts counters on every token created with Doubling Season")
    void putsCountersOnEveryDoubledToken() {
        harness.addToBattlefield(player1, new DoublingSeason());
        gd.cardsDrawnThisTurn.put(player1.getId(), 3);

        harness.castFromHand(player1, new FractalAnomaly(), "{U}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Fractal")).hasSize(2).allSatisfy(fractal -> {
            assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
            assertThat(fractal.getEffectivePower()).isEqualTo(6);
            assertThat(fractal.getEffectiveToughness()).isEqualTo(6);
        });
    }

    @Test
    @DisplayName("A second casting puts counters only on its newly created token")
    void leavesEarlierFractalUnchanged() {
        gd.cardsDrawnThisTurn.put(player1.getId(), 1);
        harness.castFromHand(player1, new FractalAnomaly(), "{U}");
        harness.passBothPriorities();
        Permanent earlier = findPermanent(player1, "Fractal");

        gd.cardsDrawnThisTurn.put(player1.getId(), 3);
        harness.castFromHand(player1, new FractalAnomaly(), "{U}");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Fractal")).hasSize(2);
        assertThat(earlier.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Fractal")).filteredOn(p -> p != earlier)
                .singleElement().satisfies(fractal ->
                        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3));
    }
}
