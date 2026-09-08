package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UrabrasksAnointerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of permanents you control with oil counters")
    void dealsDamageForOilCounterPermanents() {
        Permanent firstPermanent = harness.addToBattlefieldAndReturn(player1, new Forest());
        firstPermanent.setCounterCount(CounterType.OIL, 1);
        Permanent secondPermanent = harness.addToBattlefieldAndReturn(player1, new Swamp());
        secondPermanent.setCounterCount(CounterType.OIL, 2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castUrabrasksAnointer(player1);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts permanents rather than the number of oil counters")
    void countsPermanentsNotOilCounters() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new Forest());
        ownPermanent.setCounterCount(CounterType.OIL, 3);
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new Swamp());
        opponentPermanent.setCounterCount(CounterType.OIL, 4);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castUrabrasksAnointer(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    private void castUrabrasksAnointer(com.github.laxika.magicalvibes.model.Player player) {
        harness.setHand(player, List.of(new UrabrasksAnointer()));
        harness.addMana(player, ManaColor.RED, 4);
        harness.castCreature(player, 0);
    }
}
