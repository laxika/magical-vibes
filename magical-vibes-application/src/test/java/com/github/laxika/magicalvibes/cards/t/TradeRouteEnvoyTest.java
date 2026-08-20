package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

    private int castTradeRouteEnvoy() {
        harness.setHand(player1, List.of(new TradeRouteEnvoy()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        return gd.playerHands.get(player1.getId()).size();
    }
}
