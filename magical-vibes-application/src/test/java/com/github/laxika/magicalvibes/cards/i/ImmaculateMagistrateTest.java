package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImmaculateMagistrate.class, HillcomberGiant.class, WoodlandChangeling.class, Forest.class})
class ImmaculateMagistrateTest extends BaseCardTest {

    @Test
    @DisplayName("Puts one +1/+1 counter per Elf controlled on the target creature")
    void putsCounterPerElf() {
        // Magistrate is itself an Elf; add two more Magistrates → 3 Elves.
        addCreatureReady(player1, new ImmaculateMagistrate());
        addCreatureReady(player1, new ImmaculateMagistrate());
        addCreatureReady(player1, new ImmaculateMagistrate());
        Permanent bear = addCreatureReady(player1, new HillcomberGiant());

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counter count scales with the current number of Elves")
    void scalesWithElfCount() {
        // Only the Magistrate is an Elf → 1 counter.
        addCreatureReady(player1, new ImmaculateMagistrate());
        Permanent bear = addCreatureReady(player1, new HillcomberGiant());

        harness.activateAbility(player1, 0, null, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can target any creature, including one the opponent controls")
    void canTargetOpponentCreature() {
        addCreatureReady(player1, new ImmaculateMagistrate());
        Permanent enemyBear = addCreatureReady(player2, new HillcomberGiant());

        harness.activateAbility(player1, 0, null, enemyBear.getId());
        harness.passBothPriorities();

        assertThat(enemyBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new ImmaculateMagistrate());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countsChangelingsButNotOpponentsElves() {
        addCreatureReady(player1, new ImmaculateMagistrate());
        addCreatureReady(player1, new WoodlandChangeling());
        addCreatureReady(player2, new ImmaculateMagistrate());
        Permanent target = addCreatureReady(player1, new HillcomberGiant());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void countsElvesAddedAfterActivation() {
        addCreatureReady(player1, new ImmaculateMagistrate());
        Permanent target = addCreatureReady(player1, new HillcomberGiant());
        harness.activateAbility(player1, 0, null, target.getId());

        addCreatureReady(player1, new ImmaculateMagistrate());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void resolvesWithZeroCountersAfterLastElfLeaves() {
        Permanent source = addCreatureReady(player1, new ImmaculateMagistrate());
        Permanent target = addCreatureReady(player1, new HillcomberGiant());
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void resolvesAfterSourceLeavesAndCountsOnlyRemainingElves() {
        Permanent source = addCreatureReady(player1, new ImmaculateMagistrate());
        addCreatureReady(player1, new WoodlandChangeling());
        Permanent target = addCreatureReady(player1, new HillcomberGiant());
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canTargetItselfAndTapsAsCost() {
        Permanent source = addCreatureReady(player1, new ImmaculateMagistrate());

        harness.activateAbility(player1, 0, null, source.getId());
        assertThat(source.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ImmaculateMagistrate());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
    }
}
