package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed({BalothPackhunter.class, GrizzlyBears.class})
class BalothPackhunterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts two +1/+1 counters on each other same-name creature you control")
    void etbPutsCountersOnOtherControlledPackhunters() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BalothPackhunter());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BalothPackhunter());

        harness.setHand(player1, List.of(new BalothPackhunter()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanents(player1, "Baloth Packhunter")).hasSize(3);
        assertThat(findPermanents(player1, "Baloth Packhunter").get(2)
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("ETB does not affect another name or an opponent's creature")
    void etbOnlyAffectsOtherControlledPackhunters() {
        Permanent ownPackhunter = harness.addToBattlefieldAndReturn(player1, new BalothPackhunter());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentPackhunter = harness.addToBattlefieldAndReturn(player2, new BalothPackhunter());

        harness.setHand(player1, List.of(new BalothPackhunter()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ownPackhunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(ownBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentPackhunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
