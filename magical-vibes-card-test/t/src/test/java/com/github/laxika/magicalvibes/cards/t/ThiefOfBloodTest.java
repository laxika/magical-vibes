package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThiefOfBlood.class, GrizzlyBears.class, DarksteelIngot.class})
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

    @Test
    @DisplayName("Removes counters from noncreature permanents, including negative counters")
    void removesCountersFromNoncreaturePermanents() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        artifact.setCounterCount(CounterType.CHARGE, 3);
        artifact.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        castThiefOfBlood();

        assertThat(artifact.getTotalCounterCount()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(findPermanent(player1, "Thief of Blood")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Leaves player counters unchanged and does not count them toward its entry counters")
    void doesNotRemoveOrCountPlayerCounters() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DarksteelIngot());
        artifact.setCounterCount(CounterType.CHARGE, 2);
        gd.playerExperienceCounters.put(player1.getId(), 4);
        gd.playerPoisonCounters.put(player2.getId(), 3);

        castThiefOfBlood();

        assertThat(artifact.getTotalCounterCount()).isZero();
        assertThat(gd.playerExperienceCounters.get(player1.getId())).isEqualTo(4);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(3);
        assertThat(findPermanent(player1, "Thief of Blood")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Applies its replacement effect when entering without being cast")
    void removesCountersWhenEnteringWithoutBeingCast() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        artifact.setCounterCount(CounterType.CHARGE, 3);

        Permanent thief = harness.enterBattlefieldAndReturn(player1, new ThiefOfBlood());

        assertThat(artifact.getTotalCounterCount()).isZero();
        assertThat(thief.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    private void castThiefOfBlood() {
        harness.castFromHand(player1, new ThiefOfBlood(), "{4}{B}{B}");
        harness.passBothPriorities();
    }
}
