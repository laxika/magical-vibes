package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed({CharmedStray.class, GrizzlyBears.class})
class CharmedStrayTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on each other Charmed Stray you control")
    void putsCountersOnOtherControlledCharmedStrays() {
        Permanent existingStray = harness.addToBattlefieldAndReturn(player1, new CharmedStray());
        Permanent unrelatedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentStray = harness.addToBattlefieldAndReturn(player2, new CharmedStray());
        CharmedStray enteringCard = new CharmedStray();

        harness.setHand(player1, List.of(enteringCard));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent enteringStray = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(enteringCard.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(existingStray.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(enteringStray.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(unrelatedCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentStray.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
