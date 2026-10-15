package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeylineInvocation.class, Forest.class})
class LeylineInvocationTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Fractal with one +1/+1 counter for each land you control")
    void createsFractalWithCountersForControlledLands() {
        harness.setHand(player1, List.of(new LeylineInvocation()));
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        Permanent fractal = findPermanent(player1, "Fractal");
        assertThat(fractal.getCard().isToken()).isTrue();
        assertThat(fractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(fractal.getEffectivePower()).isEqualTo(2);
        assertThat(fractal.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The Fractal dies after resolution when its controller has no lands")
    void fractalDiesWithNoControlledLands() {
        harness.setHand(player1, List.of(new LeylineInvocation()));
        harness.addToBattlefield(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fractal");
        harness.assertInGraveyard(player1, "Leyline Invocation");
    }

    @Test
    @DisplayName("Counts lands at resolution rather than when the spell was cast")
    void countsLandsAtResolution() {
        harness.setHand(player1, List.of(new LeylineInvocation()));
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorcery(player1, 0, 0);
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Fractal").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
    }

    @Test
    @DisplayName("A second invocation puts counters only on its own new Fractal")
    void successiveCastsDoNotAddCountersToEarlierTokens() {
        harness.setHand(player1, List.of(new LeylineInvocation(), new LeylineInvocation()));
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        Permanent firstFractal = findPermanent(player1, "Fractal");

        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(firstFractal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Fractal")).hasSize(2)
                .extracting(permanent -> permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .containsExactlyInAnyOrder(1, 2);
    }
}
