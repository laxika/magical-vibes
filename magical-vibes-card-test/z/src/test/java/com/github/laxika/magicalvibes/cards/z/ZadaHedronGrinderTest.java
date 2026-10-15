package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.CommonBond;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZadaHedronGrinder.class, GrizzlyBears.class, Shock.class, CommonBond.class})
class ZadaHedronGrinderTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a spell targeting Zada for each other legal creature controlled by its caster")
    void copiesForEachOtherControlledCreature() {
        Permanent zada = harness.addToBattlefieldAndReturn(player1, new ZadaHedronGrinder());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, zada.getId());

        List<StackEntry> copies = gd.stack.stream().filter(StackEntry::isCopy).toList();
        assertThat(copies).hasSize(1);
        assertThat(copies.getFirst().getTargetId()).isEqualTo(ownCreature.getId());
        assertThat(copies.getFirst().getTargetId()).isNotEqualTo(opposingCreature.getId());
        assertThat(copies.getFirst().getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Does not trigger when an opponent casts a spell targeting Zada")
    void doesNotTriggerForOpponentCast() {
        Permanent zada = harness.addToBattlefieldAndReturn(player1, new ZadaHedronGrinder());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);

        harness.castInstant(player2, 0, zada.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when the spell targets another creature")
    void doesNotTriggerForAnotherCreature() {
        harness.addToBattlefield(player1, new ZadaHedronGrinder());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, otherCreature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Creates no copy when the caster controls no other legal creature")
    void createsNoCopyWithoutOtherControlledCreature() {
        Permanent zada = harness.addToBattlefieldAndReturn(player1, new ZadaHedronGrinder());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, zada.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
    }

    @Test
    @DisplayName("A copy of Common Bond puts both counters on its own creature")
    void retargetsEveryTargetOfTheCopy() {
        Permanent zada = harness.addToBattlefieldAndReturn(player1, new ZadaHedronGrinder());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CommonBond()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, List.of(zada.getId()));
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(zada.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(zada.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for a spell targeting both Zada and another creature")
    void doesNotTriggerForMultipleDistinctTargets() {
        Permanent zada = harness.addToBattlefieldAndReturn(player1, new ZadaHedronGrinder());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CommonBond()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, List.of(zada.getId(), bear.getId()));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(zada.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
