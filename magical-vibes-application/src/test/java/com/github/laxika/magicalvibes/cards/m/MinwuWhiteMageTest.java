package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MinwuWhiteMage.class, MasterApothecary.class, GrizzlyBears.class, AngelOfMercy.class})
class MinwuWhiteMageTest extends BaseCardTest {

    @Test
    @DisplayName("Gaining life puts a +1/+1 counter on each Cleric you control")
    void gainingLifeCountersControlledClerics() {
        Permanent minwu = harness.addToBattlefieldAndReturn(player1, new MinwuWhiteMage());
        Permanent ownCleric = harness.addToBattlefieldAndReturn(player1, new MasterApothecary());
        Permanent nonCleric = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCleric = harness.addToBattlefieldAndReturn(player2, new MasterApothecary());

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // Angel of Mercy resolves.
        harness.passBothPriorities(); // Angel of Mercy's life-gain ability resolves.
        harness.passBothPriorities(); // Minwu's triggered ability resolves.

        assertThat(minwu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCleric.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonCleric.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCleric.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
