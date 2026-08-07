package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConsulsLieutenantTest extends BaseCardTest {

    @Test
    @DisplayName("Renown 1 puts a +1/+1 counter on it after unblocked combat damage")
    void renownOnCombatDamage() {
        Permanent lieutenant = addCreatureReady(player1, new ConsulsLieutenant());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(lieutenant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(lieutenant.isRenowned()).isTrue();
    }

    @Test
    @DisplayName("Renown does nothing when the creature is already renowned")
    void renownOnlyOnce() {
        Permanent lieutenant = addCreatureReady(player1, new ConsulsLieutenant());
        lieutenant.setRenowned(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(lieutenant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Attacking while not renowned does not pump other attackers")
    void noPumpWhenNotRenowned() {
        addCreatureReady(player1, new ConsulsLieutenant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Attacking while renowned gives other attacking creatures +1/+1")
    void pumpsOtherAttackersWhenRenowned() {
        Permanent lieutenant = addCreatureReady(player1, new ConsulsLieutenant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        lieutenant.setRenowned(true);

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);
        assertThat(lieutenant.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("A non-attacking creature you control is not pumped")
    void doesNotPumpNonAttackers() {
        Permanent lieutenant = addCreatureReady(player1, new ConsulsLieutenant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        lieutenant.setRenowned(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(bears.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The +1/+1 boost wears off at end of turn")
    void boostWearsOff() {
        Permanent lieutenant = addCreatureReady(player1, new ConsulsLieutenant());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        lieutenant.setRenowned(true);

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(bears.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
    }
}
