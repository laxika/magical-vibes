package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GruulNodorog;
import com.github.laxika.magicalvibes.cards.g.GruulScrapper;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BatteringWurm.class, GruulNodorog.class, GruulScrapper.class})
class BatteringWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 1 puts a +1/+1 counter on Battering Wurm after an opponent was dealt damage")
    void bloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);

        Permanent wurm = castWurm();

        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bloodthirst 1 does not apply when no opponent was dealt damage")
    void bloodthirstDoesNotApply() {
        Permanent wurm = castWurm();

        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst 1 ignores damage dealt to Battering Wurm's controller")
    void bloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 1);

        Permanent wurm = castWurm();

        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A creature with less power cannot block Battering Wurm")
    void lowerPowerCreatureCannotBlock() {
        addCreatureReady(player1, new BatteringWurm());
        Permanent blocker = addCreatureReady(player2, new GruulScrapper());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A creature with equal power can block Battering Wurm")
    void equalPowerCreatureCanBlock() {
        addCreatureReady(player1, new BatteringWurm());
        Permanent blocker = addCreatureReady(player2, new GruulNodorog());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Bloodthirst raises Battering Wurm's blocking threshold")
    void bloodthirstRaisesBlockingThreshold() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        Permanent wurm = castWurm();
        wurm.setSummoningSick(false);
        Permanent blocker = addCreatureReady(player2, new GruulNodorog());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Bloodthirst still adds only one counter after multiple points of damage")
    void bloodthirstDoesNotScaleWithDamage() {
        gd.recordDamageToPlayer(player2.getId(), 5);

        Permanent wurm = castWurm();

        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bloodthirst applies when Battering Wurm enters without being cast")
    void bloodthirstAppliesWithoutCasting() {
        gd.recordDamageToPlayer(player2.getId(), 1);

        Permanent wurm = harness.enterBattlefieldAndReturn(player1, new BatteringWurm());

        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A blocker boosted above Battering Wurm's power can block it")
    void greaterEffectivePowerCreatureCanBlock() {
        addCreatureReady(player1, new BatteringWurm());
        Permanent blocker = addCreatureReady(player2, new GruulScrapper());

        declareAttackersAndPrepareBlockers(List.of(0));
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Reducing Battering Wurm's power before blocks lowers its blocking threshold")
    void reducedEffectivePowerLowersBlockingThreshold() {
        Permanent wurm = addCreatureReady(player1, new BatteringWurm());
        Permanent blocker = addCreatureReady(player2, new GruulScrapper());

        declareAttackersAndPrepareBlockers(List.of(0));
        wurm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent castWurm() {
        harness.castFromHand(player1, new BatteringWurm(), "{6}{G}");
        resolveAllTriggers();
        return findPermanent(player1, "Battering Wurm");
    }
}
