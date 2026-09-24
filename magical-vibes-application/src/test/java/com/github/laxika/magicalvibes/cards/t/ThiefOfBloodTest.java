package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({ThiefOfBlood.class, GrizzlyBears.class})
class ThiefOfBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Removes all counters from all permanents and enters with that many +1/+1 counters")
    void removesAllCountersAndEntersWithMatchingCounters() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ownPermanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        ownPermanent.setCounterCount(CounterType.CHARGE, 1);
        opposingPermanent.setCounterCount(CounterType.LOYALTY, 3);

        castThiefOfBlood();

        assertThat(ownPermanent.getTotalCounterCount()).isZero();
        assertThat(opposingPermanent.getTotalCounterCount()).isZero();
        assertThat(findPermanent(player1, "Thief of Blood")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Enters without counters when no permanent has counters")
    void entersWithoutCountersWhenNoneArePresent() {
        castThiefOfBlood();

        assertThat(findPermanent(player1, "Thief of Blood")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castThiefOfBlood() {
        harness.setHand(player1, List.of(new ThiefOfBlood()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
