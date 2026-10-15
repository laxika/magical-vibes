package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.c.CudgelTroll;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DivineVerdict.class, RuneclawBear.class, CudgelTroll.class})
class DivineVerdictTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Divine Verdict targeting an attacking creature puts it on the stack")
    void castingTargetingAttackingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DivineVerdict()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Casting Divine Verdict targeting a blocking creature puts it on the stack")
    void castingTargetingBlockingCreature() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DivineVerdict()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, blocker.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(blocker.getId());
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        // Add an attacking creature as valid target so spell is playable
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.addToBattlefield(player2, new RuneclawBear());
        UUID targetId = harness.getPermanentId(player2, "Runeclaw Bear");

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DivineVerdict()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        // Add an attacking creature as valid target so spell is playable
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DivineVerdict()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("This spell cannot target players");
    }

    @Test
    @DisplayName("Resolving destroys the attacking creature")
    void resolvingDestroysAttackingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DivineVerdict()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, attacker.getId());

        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Resolving destroys the blocking creature")
    void resolvingDestroysBlockingCreature() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DivineVerdict()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, blocker.getId());

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Divine Verdict goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DivineVerdict()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.passPriority(player1);

        harness.castAndResolveInstant(player2, 0, attacker.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Divine Verdict");
    }

    @Test
    @DisplayName("Divine Verdict fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DivineVerdict()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.passPriority(player1);

        harness.castInstant(player2, 0, attacker.getId());

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        // Divine Verdict still goes to graveyard
        harness.assertInGraveyard(player2, "Divine Verdict");
    }

    @Test
    @DisplayName("Target leaving combat before resolution survives")
    void targetLeavingCombatSurvives() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new DivineVerdict()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, attacker.getId());

        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Runeclaw Bear");
        harness.assertNotInGraveyard(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Divine Verdict");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A blocker leaving combat before resolution survives")
    void blockerLeavingCombatSurvives() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        blocker.setBlocking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player2, List.of(new DivineVerdict()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, blocker.getId());

        blocker.setBlocking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Runeclaw Bear");
        harness.assertNotInGraveyard(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Divine Verdict");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An attacking creature remains a valid target during the end of combat step")
    void destroysAttackerDuringEndOfCombat() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.setHand(player1, List.of(new DivineVerdict()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0, attacker.getId());

        harness.assertNotOnBattlefield(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player1, "Divine Verdict");
    }

    @Test
    @DisplayName("Regeneration in response saves the attacker and removes it from combat")
    void regenerationSavesAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new CudgelTroll());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new DivineVerdict()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, attacker.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cudgel Troll");
        harness.assertNotInGraveyard(player1, "Cudgel Troll");
        assertThat(attacker.isTapped()).isTrue();
        assertThat(attacker.isAttacking()).isFalse();
        harness.assertInGraveyard(player2, "Divine Verdict");
    }
}
