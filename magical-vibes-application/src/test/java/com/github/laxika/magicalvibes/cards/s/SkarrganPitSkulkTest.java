package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
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

@CardUsed({SkarrganPitSkulk.class, DarksteelMyr.class, GrizzlyBears.class})
class SkarrganPitSkulkTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodthirst 1: enters with a +1/+1 counter when an opponent was dealt damage")
    void bloodthirstApplies() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castSkulk();

        assertThat(findPermanent(player1, "Skarrgan Pit-Skulk")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bloodthirst 1: enters without a counter when no opponent was dealt damage")
    void bloodthirstDoesNotApply() {
        castSkulk();

        assertThat(findPermanent(player1, "Skarrgan Pit-Skulk")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst 1 ignores damage dealt to its own controller")
    void bloodthirstIgnoresControllerDamage() {
        gd.recordDamageToPlayer(player1.getId(), 1);
        castSkulk();

        assertThat(findPermanent(player1, "Skarrgan Pit-Skulk")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A creature with less power cannot block it")
    void lowerPowerCreatureCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new DarksteelMyr());
        Permanent skulk = addCreatureReady(player1, new SkarrganPitSkulk());
        skulk.setAttacking(true);

        prepareDeclareBlockers(player1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(skulk);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power too low");
    }

    @Test
    @DisplayName("A creature with equal power can block it")
    void equalPowerCreatureCanBlock() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castSkulk();

        Permanent skulk = findPermanent(player1, "Skarrgan Pit-Skulk");
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        skulk.setAttacking(true);

        prepareDeclareBlockers(player1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(skulk);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void castSkulk() {
        harness.setHand(player1, List.of(new SkarrganPitSkulk()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
