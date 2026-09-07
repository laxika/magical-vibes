package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MirriTheCursed.class, GrizzlyBears.class})
class MirriTheCursedTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when it deals combat damage to a creature")
    void getsCounterWhenDealingCombatDamageToCreature() {
        Permanent mirri = addCreatureReady(player1, new MirriTheCursed());
        mirri.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(mirri.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a counter when it deals combat damage to a player")
    void doesNotGetCounterWhenDealingCombatDamageToPlayer() {
        Permanent mirri = addCreatureReady(player1, new MirriTheCursed());
        mirri.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mirri.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
