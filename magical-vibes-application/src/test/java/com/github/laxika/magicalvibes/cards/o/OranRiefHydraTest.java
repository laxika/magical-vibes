package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OranRiefHydra.class, Forest.class, Mountain.class})
class OranRiefHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Forest landfall puts two +1/+1 counters on Oran-Rief Hydra")
    void forestLandfallPutsTwoCounters() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new OranRiefHydra());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-Forest landfall puts one +1/+1 counter on Oran-Rief Hydra")
    void nonForestLandfallPutsOneCounter() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new OranRiefHydra());
        harness.setHand(player1, List.of(new Mountain()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Oran-Rief Hydra")
    void opponentLandDoesNotTrigger() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new OranRiefHydra());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
