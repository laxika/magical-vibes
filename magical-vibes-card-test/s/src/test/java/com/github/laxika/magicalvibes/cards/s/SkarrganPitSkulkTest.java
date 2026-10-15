package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GhorClanSavage;
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

@CardUsed({SkarrganPitSkulk.class, SilhanaStarfletcher.class, GhorClanSavage.class,
        GruulScrapper.class})
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
    @DisplayName("Bloodthirst counts damage dealt after casting but before it enters")
    void bloodthirstCountsDamageBeforeResolution() {
        harness.castFromHand(player1, new SkarrganPitSkulk(), "{G}");
        gd.recordDamageToPlayer(player2.getId(), 1);
        resolveAllTriggers();

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
    @DisplayName("Bloodthirst does not count life loss without damage")
    void bloodthirstIgnoresLifeLoss() {
        gd.playerLifeTotals.put(player2.getId(), 19);
        castSkulk();

        assertThat(findPermanent(player1, "Skarrgan Pit-Skulk")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bloodthirst 1 adds only one counter regardless of damage amount")
    void bloodthirstCounterCountDoesNotScaleWithDamage() {
        gd.recordDamageToPlayer(player2.getId(), 5);
        gd.recordDamageToPlayer(player2.getId(), 3);
        castSkulk();

        assertThat(findPermanent(player1, "Skarrgan Pit-Skulk")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocking restriction uses the blocker's power including counters")
    void blockerWithCounterCanReachRequiredPower() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castSkulk();

        Permanent skulk = findPermanent(player1, "Skarrgan Pit-Skulk");
        Permanent blocker = addCreatureReady(player2, new SilhanaStarfletcher());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        skulk.setAttacking(true);
        prepareDeclareBlockers(player1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(skulk);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature with less power cannot block it")
    void lowerPowerCreatureCannotBlock() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castSkulk();

        Permanent blocker = addCreatureReady(player2, new SilhanaStarfletcher());
        Permanent skulk = findPermanent(player1, "Skarrgan Pit-Skulk");
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
        Permanent blocker = addCreatureReady(player2, new GhorClanSavage());
        skulk.setAttacking(true);

        prepareDeclareBlockers(player1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(skulk);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature with greater power can block it")
    void greaterPowerCreatureCanBlock() {
        gd.recordDamageToPlayer(player2.getId(), 1);
        castSkulk();

        Permanent skulk = findPermanent(player1, "Skarrgan Pit-Skulk");
        Permanent blocker = addCreatureReady(player2, new GruulScrapper());
        skulk.setAttacking(true);

        prepareDeclareBlockers(player1);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(skulk);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void castSkulk() {
        harness.castFromHand(player1, new SkarrganPitSkulk(), "{G}");
        resolveAllTriggers();
    }
}
