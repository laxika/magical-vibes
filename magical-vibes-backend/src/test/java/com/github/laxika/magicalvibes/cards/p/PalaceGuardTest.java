package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PalaceGuardTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Palace Guard has CanBlockAnyNumberOfCreaturesEffect as static effect")
    void hasCorrectStaticEffect() {
        PalaceGuard card = new PalaceGuard();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(CanBlockAnyNumberOfCreaturesEffect.class);
    }

    // ===== Blocking multiple attackers =====

    @Test
    @DisplayName("Palace Guard can block three attackers at once")
    void canBlockThreeAttackers() {
        PalaceGuard guard = new PalaceGuard();
        Permanent guardPerm = new Permanent(guard);
        guardPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(guardPerm);

        for (int i = 0; i < 3; i++) {
            GrizzlyBears atk = new GrizzlyBears();
            Permanent atkPerm = new Permanent(atk);
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atkPerm);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1),
                new BlockerAssignment(0, 2)
        ));

        assertThat(guardPerm.isBlocking()).isTrue();
        assertThat(guardPerm.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2);
    }

    @Test
    @DisplayName("Palace Guard can block five attackers at once")
    void canBlockFiveAttackers() {
        PalaceGuard guard = new PalaceGuard();
        Permanent guardPerm = new Permanent(guard);
        guardPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(guardPerm);

        for (int i = 0; i < 5; i++) {
            GrizzlyBears atk = new GrizzlyBears();
            Permanent atkPerm = new Permanent(atk);
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atkPerm);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1),
                new BlockerAssignment(0, 2),
                new BlockerAssignment(0, 3),
                new BlockerAssignment(0, 4)
        ));

        assertThat(guardPerm.isBlocking()).isTrue();
        assertThat(guardPerm.getBlockingTargets()).hasSize(5);
    }

    // ===== Combat damage with multi-block =====

    @Test
    @DisplayName("Palace Guard (1/4) survives blocking three 1/1 attackers")
    void survivesBlockingThreeSmallAttackers() {
        PalaceGuard guard = new PalaceGuard();
        Permanent guardPerm = new Permanent(guard);
        guardPerm.setSummoningSick(false);
        guardPerm.setBlocking(true);
        guardPerm.addBlockingTarget(0);
        guardPerm.addBlockingTarget(1);
        guardPerm.addBlockingTarget(2);
        gd.playerBattlefields.get(player2.getId()).add(guardPerm);

        for (int i = 0; i < 3; i++) {
            GrizzlyBears atk = new GrizzlyBears();
            atk.setPower(1);
            atk.setToughness(1);
            Permanent atkPerm = new Permanent(atk);
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atkPerm);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Palace Guard takes 3 damage total (3x 1/1) — survives as 1/4
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Palace Guard"));
        // First attacker killed by Palace Guard's 1 power
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Palace Guard (1/4) dies when blocking four 1/1 attackers")
    void diesBlockingFourSmallAttackers() {
        PalaceGuard guard = new PalaceGuard();
        Permanent guardPerm = new Permanent(guard);
        guardPerm.setSummoningSick(false);
        guardPerm.setBlocking(true);
        guardPerm.addBlockingTarget(0);
        guardPerm.addBlockingTarget(1);
        guardPerm.addBlockingTarget(2);
        guardPerm.addBlockingTarget(3);
        gd.playerBattlefields.get(player2.getId()).add(guardPerm);

        for (int i = 0; i < 4; i++) {
            GrizzlyBears atk = new GrizzlyBears();
            atk.setPower(1);
            atk.setToughness(1);
            Permanent atkPerm = new Permanent(atk);
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atkPerm);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Palace Guard takes 4 damage (4x 1/1) — dies (toughness 4)
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Palace Guard"));
        // Palace Guard kills first attacker with 1 power
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Blocked attackers deal no damage to defending player")
    void blockedAttackersDealNoDamageToPlayer() {
        harness.setLife(player2, 20);

        PalaceGuard guard = new PalaceGuard();
        Permanent guardPerm = new Permanent(guard);
        guardPerm.setSummoningSick(false);
        guardPerm.setBlocking(true);
        guardPerm.addBlockingTarget(0);
        guardPerm.addBlockingTarget(1);
        gd.playerBattlefields.get(player2.getId()).add(guardPerm);

        for (int i = 0; i < 2; i++) {
            GrizzlyBears atk = new GrizzlyBears();
            Permanent atkPerm = new Permanent(atk);
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(atkPerm);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // Both attackers are blocked — no damage to player
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
