package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SpiritOfTheLabyrinth;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TradeRouteEnvoy.class, GrizzlyBears.class, SpiritOfTheLabyrinth.class})
class TradeRouteEnvoyTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws a card when you control a creature with a counter")
    void etbDrawsWithCounterCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.CHARGE, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = castTradeRouteEnvoy();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(findPermanent(player1, "Trade Route Envoy").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    @DisplayName("ETB puts a +1/+1 counter on itself without a creature with a counter")
    void etbPutsCounterWithoutCounterCreature() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = castTradeRouteEnvoy();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(findPermanent(player1, "Trade Route Envoy").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("ETB ignores an opponent's creature with a counter")
    void etbIgnoresOpponentCounterCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setCounterCount(CounterType.CHARGE, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = castTradeRouteEnvoy();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(findPermanent(player1, "Trade Route Envoy").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("ETB puts a counter on itself when the qualifying draw is prohibited")
    void etbPutsCounterWhenDrawIsProhibited() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new SpiritOfTheLabyrinth());
        spirit.setCounterCount(CounterType.CHARGE, 1);
        harness.setLibrary(player1, List.of(new TradeRouteEnvoy(), new TradeRouteEnvoy()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        int handBefore = castTradeRouteEnvoy();

        harness.passBothPriorities();
        Permanent envoy = findPermanent(player1, "Trade Route Envoy");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(envoy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Envoy itself can satisfy the counter condition at resolution")
    void etbCountsItsOwnCounter() {
        harness.setLibrary(player1, List.of(new TradeRouteEnvoy()));
        int handBefore = castTradeRouteEnvoy();
        harness.passBothPriorities();
        Permanent envoy = findPermanent(player1, "Trade Route Envoy");
        envoy.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(envoy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A counter gained after entry qualifies for the draw")
    void etbChecksNewCountersAtResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = castTradeRouteEnvoy();
        harness.passBothPriorities();
        creature.setCounterCount(CounterType.CHARGE, 1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(findPermanent(player1, "Trade Route Envoy").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    @DisplayName("Losing the last qualifying counter before resolution gives Envoy a counter")
    void etbChecksRemovedCountersAtResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.CHARGE, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        int handBefore = castTradeRouteEnvoy();
        harness.passBothPriorities();
        creature.setCounterCount(CounterType.CHARGE, 0);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(findPermanent(player1, "Trade Route Envoy").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    private int castTradeRouteEnvoy() {
        harness.castFromHand(player1, new TradeRouteEnvoy(), "{3}{G}");
        return gd.playerHands.get(player1.getId()).size();
    }
}
