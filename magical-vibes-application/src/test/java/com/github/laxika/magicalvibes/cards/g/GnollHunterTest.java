package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GnollHunter.class, GrizzlyBears.class})
class GnollHunterTest extends BaseCardTest {

    @Test
    void getsCounterWhenAttackingCreaturesHaveTotalPowerAtLeastSix() {
        Permanent hunter = addCreatureReady(player1, new GnollHunter());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotGetCounterWhenAttackingCreaturesHaveTotalPowerLessThanSix() {
        Permanent hunter = addCreatureReady(player1, new GnollHunter());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void packTacticsRequiresGnollHunterToAttack() {
        Permanent hunter = addCreatureReady(player1, new GnollHunter());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(hunter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
