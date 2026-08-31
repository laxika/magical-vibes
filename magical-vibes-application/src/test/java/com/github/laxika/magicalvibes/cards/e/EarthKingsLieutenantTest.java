package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KyoshiWarriors;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EarthKingsLieutenant.class, KyoshiWarriors.class, GrizzlyBears.class})
class EarthKingsLieutenantTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a counter on each other Ally creature when it enters")
    void putsCounterOnEachOtherAllyWhenEntering() {
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new KyoshiWarriors());
        Permanent nonAlly = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new EarthKingsLieutenant()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent lieutenant = findPermanent(player1, "Earth King's Lieutenant");
        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(lieutenant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(nonAlly.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Puts a counter on itself for each other Ally that enters")
    void putsCounterOnItselfForEachOtherAllyEntering() {
        Permanent lieutenant = addCreatureReady(player1, new EarthKingsLieutenant());
        harness.setHand(player1, List.of(new KyoshiWarriors()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(lieutenant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(lieutenant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
