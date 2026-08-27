package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BatteringWurm.class, AirElemental.class, GrizzlyBears.class})
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
        Permanent wurm = addCreatureReady(player1, new BatteringWurm());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        wurm.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
        assertThat(blocker.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("A creature with equal power can block Battering Wurm")
    void equalPowerCreatureCanBlock() {
        Permanent wurm = addCreatureReady(player1, new BatteringWurm());
        Permanent blocker = addCreatureReady(player2, new AirElemental());
        wurm.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Bloodthirst raises Battering Wurm's blocking threshold")
    void bloodthirstRaisesBlockingThreshold() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        Permanent wurm = castWurm();
        Permanent blocker = addCreatureReady(player2, new AirElemental());
        wurm.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
        assertThat(blocker.isBlocking()).isFalse();
    }

    private Permanent castWurm() {
        harness.setHand(player1, List.of(new BatteringWurm()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        return findPermanent(player1, "Battering Wurm");
    }
}
