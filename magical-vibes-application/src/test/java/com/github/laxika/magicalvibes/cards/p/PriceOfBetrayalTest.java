package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PriceOfBetrayal.class, GrizzlyBears.class})
class PriceOfBetrayalTest extends BaseCardTest {

    @Test
    void removesChosenCountersOfDifferentKindsFromTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        target.setCounterCount(CounterType.CHARGE, 2);

        castPriceOfBetrayal(target.getId());

        harness.handleListChoice(player1, "2");
        harness.handleListChoice(player1, "1");

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void removesUpToFivePoisonAndEnergyCountersFromTargetOpponent() {
        gd.playerPoisonCounters.put(player2.getId(), 4);
        gd.playerEnergyCounters.put(player2.getId(), 3);

        castPriceOfBetrayal(player2.getId());

        harness.handleListChoice(player1, "4");
        harness.handleListChoice(player1, "1");

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isZero();
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(2);
    }

    @Test
    void cannotTargetItsController() {
        assertThatThrownBy(() -> castPriceOfBetrayal(player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castPriceOfBetrayal(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new PriceOfBetrayal()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
