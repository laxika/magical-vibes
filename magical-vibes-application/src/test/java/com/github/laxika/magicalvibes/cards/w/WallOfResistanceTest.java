package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WallOfResistanceTest extends BaseCardTest {

    /** Simulates the Wall having been dealt damage this turn. */
    private void recordDamageDealtTo(Permanent permanent) {
        gd.recordDamageToPermanent(permanent.getId(), 1);
    }

    private void advanceToEndStepAndResolve(UUID activePlayerId) {
        harness.forceActivePlayer(activePlayerId.equals(player1.getId()) ? player1 : player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gets a +0/+1 counter at end step after being dealt damage")
    void getsCounterAfterBeingDamaged() {
        Permanent wall = new Permanent(new WallOfResistance());
        gd.playerBattlefields.get(player1.getId()).add(wall);

        recordDamageDealtTo(wall);

        advanceToEndStepAndResolve(player1.getId());

        assertThat(wall.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, wall)).isZero();
    }

    @Test
    @DisplayName("Gets no counter when it wasn't dealt damage this turn")
    void noCounterWithoutDamage() {
        Permanent wall = new Permanent(new WallOfResistance());
        gd.playerBattlefields.get(player1.getId()).add(wall);

        advanceToEndStepAndResolve(player1.getId());

        assertThat(wall.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers at each end step, including an opponent's")
    void triggersOnOpponentEndStep() {
        Permanent wall = new Permanent(new WallOfResistance());
        gd.playerBattlefields.get(player1.getId()).add(wall);

        recordDamageDealtTo(wall);

        advanceToEndStepAndResolve(player2.getId());

        assertThat(wall.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isEqualTo(1);
    }
}
