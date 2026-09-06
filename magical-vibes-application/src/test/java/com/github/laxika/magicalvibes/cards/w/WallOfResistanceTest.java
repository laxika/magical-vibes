package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.u.UnyaroBeeSting;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WallOfResistance.class)
class WallOfResistanceTest extends BaseCardTest {

    /** Simulates the Wall having been dealt damage this turn. */
    private void recordDamageDealtTo(Permanent permanent) {
        gd.recordDamageToPermanent(permanent.getId(), 1);
    }

    private void advanceToEndStepAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Gets a +0/+1 counter at end step after being dealt damage")
    void getsCounterAfterBeingDamaged() {
        Permanent wall = new Permanent(new WallOfResistance());
        gd.playerBattlefields.get(player1.getId()).add(wall);

        recordDamageDealtTo(wall);

        advanceToEndStepAndResolve(player1);

        assertThat(wall.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, wall)).isZero();
    }

    @Test
    @CardUsed(UnyaroBeeSting.class)
    @DisplayName("Gets a +0/+1 counter after being dealt noncombat damage")
    void getsCounterAfterBeingDealtNoncombatDamage() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfResistance());
        harness.setHand(player1, List.of(new UnyaroBeeSting()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, wall.getId());
        advanceToEndStepAndResolve(player1);

        assertThat(wall.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets no counter when it wasn't dealt damage this turn")
    void noCounterWithoutDamage() {
        Permanent wall = new Permanent(new WallOfResistance());
        gd.playerBattlefields.get(player1.getId()).add(wall);

        advanceToEndStepAndResolve(player1);

        assertThat(wall.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers at each end step, including an opponent's")
    void triggersOnOpponentEndStep() {
        Permanent wall = new Permanent(new WallOfResistance());
        gd.playerBattlefields.get(player1.getId()).add(wall);

        recordDamageDealtTo(wall);

        advanceToEndStepAndResolve(player2);

        assertThat(wall.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isEqualTo(1);
    }
}
