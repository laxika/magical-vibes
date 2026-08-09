package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SlithAscendantTest extends BaseCardTest {

    private Permanent addReadySlithAscendant() {
        Permanent slith = new Permanent(new SlithAscendant());
        slith.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(slith);
        return slith;
    }

    @Test
    @DisplayName("Gets a +1/+1 counter when dealing combat damage to a player")
    void getsCounterOnCombatDamageToPlayer() {
        Permanent slith = addReadySlithAscendant();
        slith.setAttacking(true);
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        harness.passBothPriorities();

        assertThat(slith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not get a counter when blocked")
    void noCounterWhenBlocked() {
        Permanent slith = addReadySlithAscendant();
        slith.setAttacking(true);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(slith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
