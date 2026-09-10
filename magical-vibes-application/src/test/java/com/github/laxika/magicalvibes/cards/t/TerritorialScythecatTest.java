package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TerritorialScythecat.class, Forest.class})
class TerritorialScythecatTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall puts a +1/+1 counter on Territorial Scythecat")
    void landfallPutsCounterOnSelf() {
        Permanent scythecat = harness.addToBattlefieldAndReturn(player1, new TerritorialScythecat());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(scythecat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Territorial Scythecat")
    void opponentLandDoesNotTrigger() {
        Permanent scythecat = harness.addToBattlefieldAndReturn(player1, new TerritorialScythecat());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(scythecat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
