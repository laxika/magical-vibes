package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.cards.s.StormfrontPegasus;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PalaceGuard.class, RuneclawBear.class, SoulWarden.class, StormfrontPegasus.class})
class PalaceGuardTest extends BaseCardTest {


    @Test
    @DisplayName("Palace Guard can block three attackers at once")
    void canBlockThreeAttackers() {
        Permanent guardPerm = harness.addToBattlefieldAndReturn(player2, new PalaceGuard());
        guardPerm.setSummoningSick(false);

        for (int i = 0; i < 3; i++) {
            Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

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
        Permanent guardPerm = harness.addToBattlefieldAndReturn(player2, new PalaceGuard());
        guardPerm.setSummoningSick(false);

        for (int i = 0; i < 5; i++) {
            Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

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


    @Test
    @DisplayName("Palace Guard (1/4) survives blocking three 1/1 attackers")
    void survivesBlockingThreeSmallAttackers() {
        Permanent guardPerm = harness.addToBattlefieldAndReturn(player2, new PalaceGuard());
        guardPerm.setSummoningSick(false);
        guardPerm.setBlocking(true);
        guardPerm.addBlockingTarget(0);
        guardPerm.addBlockingTarget(1);
        guardPerm.addBlockingTarget(2);

        for (int i = 0; i < 3; i++) {
            Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new SoulWarden());
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // CR 510.1d — Palace Guard blocks 3 attackers, so its controller divides its 1 combat
        // damage; put it on the first attacker.
        harness.handleCombatDamageAssigned(player2, 0, java.util.Map.of(
                gd.playerBattlefields.get(player1.getId()).get(0).getId(), 1));

        // Palace Guard takes 3 damage total (3x 1/1) — survives as 1/4
        harness.assertOnBattlefield(player2, "Palace Guard");
        // First attacker killed by Palace Guard's 1 power
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Palace Guard (1/4) dies when blocking four 1/1 attackers")
    void diesBlockingFourSmallAttackers() {
        Permanent guardPerm = harness.addToBattlefieldAndReturn(player2, new PalaceGuard());
        guardPerm.setSummoningSick(false);
        guardPerm.setBlocking(true);
        guardPerm.addBlockingTarget(0);
        guardPerm.addBlockingTarget(1);
        guardPerm.addBlockingTarget(2);
        guardPerm.addBlockingTarget(3);

        for (int i = 0; i < 4; i++) {
            Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new SoulWarden());
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // CR 510.1d — Palace Guard blocks 4 attackers, so its controller divides its 1 combat
        // damage; put it on the first attacker.
        harness.handleCombatDamageAssigned(player2, 0, java.util.Map.of(
                gd.playerBattlefields.get(player1.getId()).get(0).getId(), 1));

        // Palace Guard takes 4 damage (4x 1/1) — dies (toughness 4)
        harness.assertInGraveyard(player2, "Palace Guard");
        // Palace Guard kills first attacker with 1 power
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Blocked attackers deal no damage to defending player")
    void blockedAttackersDealNoDamageToPlayer() {
        harness.setLife(player2, 20);

        Permanent guardPerm = harness.addToBattlefieldAndReturn(player2, new PalaceGuard());
        guardPerm.setSummoningSick(false);
        guardPerm.setBlocking(true);
        guardPerm.addBlockingTarget(0);
        guardPerm.addBlockingTarget(1);

        for (int i = 0; i < 2; i++) {
            Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
            atkPerm.setSummoningSick(false);
            atkPerm.setAttacking(true);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player2, 0, java.util.Map.of(
                gd.playerBattlefields.get(player1.getId()).get(1).getId(), 1));
        harness.assertInGraveyard(player2, "Palace Guard");
        assertThat(gd.playerBattlefields.get(player1.getId()).get(0).getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).getMarkedDamage()).isEqualTo(1);

        // Both attackers are blocked — no damage to player
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Palace Guard cannot block a flying attacker alongside a ground attacker")
    void cannotBlockFlyingAttacker() {
        Permanent guard = harness.addToBattlefieldAndReturn(player2, new PalaceGuard());
        Permanent ground = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        Permanent flying = harness.addToBattlefieldAndReturn(player1, new StormfrontPegasus());
        ground.setAttacking(true);
        flying.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(guard.isBlocking()).isFalse();
        assertThat(guard.getBlockingTargets()).isEmpty();
    }

    @Test
    @DisplayName("Tapped Palace Guard cannot block multiple attackers")
    void tappedGuardCannotBlock() {
        Permanent guard = harness.addToBattlefieldAndReturn(player2, new PalaceGuard());
        guard.setTapped(true);
        for (int i = 0; i < 2; i++) {
            harness.addToBattlefieldAndReturn(player1, new RuneclawBear()).setAttacking(true);
        }
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class);
        assertThat(guard.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Summoning sick Palace Guard may block multiple attackers")
    void summoningSickGuardCanBlock() {
        Permanent guard = harness.addToBattlefieldAndReturn(player2, new PalaceGuard());
        guard.setSummoningSick(true);
        for (int i = 0; i < 2; i++) {
            harness.addToBattlefieldAndReturn(player1, new RuneclawBear()).setAttacking(true);
        }
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));

        assertThat(guard.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }
}
