package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DefendTheCelestus.class, GrizzlyBears.class})
class DefendTheCelestusTest extends BaseCardTest {

    @Test
    void distributesOneCounterToEachOfThreeTargets() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new DefendTheCelestus()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        Permanent bears1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bears2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bears3 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castInstant(player1, 0, Map.of(
                bears1.getId(), 1,
                bears2.getId(), 1,
                bears3.getId(), 1
        ));
        harness.passBothPriorities();

        assertThat(bears1.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears2.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears3.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void allowsUnevenDistributionAmongTwoTargets() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new DefendTheCelestus()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        Permanent bears1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent bears2 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castInstant(player1, 0, Map.of(bears1.getId(), 2, bears2.getId(), 1));
        harness.passBothPriorities();

        assertThat(bears1.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears2.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void rejectsCreatureControlledByOpponent() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new DefendTheCelestus()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, Map.of(opponentBears.getId(), 3)))
                .isInstanceOf(IllegalStateException.class);
    }
}
