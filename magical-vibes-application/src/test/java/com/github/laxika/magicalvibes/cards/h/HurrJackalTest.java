package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HurrJackalTest extends BaseCardTest {

    // ===== Activation =====

    @Test
    @DisplayName("Activating the ability puts it on the stack targeting the creature and taps Hurr Jackal")
    void activatingPutsAbilityOnStack() {
        Permanent jackal = addReadyJackal(player1);
        Permanent skele = addRegeneratingSkeleton(player2);

        harness.activateAbility(player1, 0, null, skele.getId());

        assertThat(jackal.isTapped()).isTrue();
        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Hurr Jackal");
        assertThat(entry.getTargetId()).isEqualTo(skele.getId());
    }

    // ===== Resolution marks the creature =====

    @Test
    @DisplayName("Resolving marks the target so it can't be regenerated, leaving its shield intact")
    void resolvingMarksTargetCantBeRegenerated() {
        addReadyJackal(player1);
        Permanent skele = addRegeneratingSkeleton(player2);

        harness.activateAbility(player1, 0, null, skele.getId());
        harness.passBothPriorities();

        assertThat(skele.isCantRegenerateThisTurn()).isTrue();
        // The mark does not touch the shield — it blocks regeneration outright.
        assertThat(skele.getRegenerationShield()).isEqualTo(1);
    }

    // ===== Prevents regeneration =====

    @Test
    @DisplayName("A marked creature dies in combat despite its regeneration shield")
    void markedCreatureDiesInCombatDespiteShield() {
        // Mark the opponent's regenerating skeleton (clean stack while resolving the ability).
        addReadyJackal(player1);
        Permanent skele = addRegeneratingSkeleton(player2);

        harness.activateAbility(player1, 0, null, skele.getId());
        harness.passBothPriorities();

        // Now the marked skeleton blocks a lethal attacker.
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(bears);
        skele.setBlocking(true);
        skele.addBlockingTargetId(bears.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Drudge Skeletons"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Drudge Skeletons"));
    }

    @Test
    @DisplayName("Without Hurr Jackal's mark, the same creature regenerates and survives combat")
    void withoutMarkCreatureRegeneratesInCombat() {
        Permanent skele = addRegeneratingSkeleton(player2);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(bears);
        skele.setBlocking(true);
        skele.addBlockingTargetId(bears.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Drudge Skeletons"));
        assertThat(skele.getRegenerationShield()).isEqualTo(0);
    }

    // ===== Mark wears off =====

    @Test
    @DisplayName("The can't-be-regenerated mark clears during end-of-turn cleanup")
    void markClearsAtEndOfTurn() {
        addReadyJackal(player1);
        Permanent skele = addRegeneratingSkeleton(player2);

        harness.activateAbility(player1, 0, null, skele.getId());
        harness.passBothPriorities();
        assertThat(skele.isCantRegenerateThisTurn()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances END -> CLEANUP

        assertThat(skele.isCantRegenerateThisTurn()).isFalse();
    }

    // ===== Illegal targets =====

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReadyJackal(player1);
        harness.addToBattlefield(player1, new FountainOfYouth());

        UUID fountainId = harness.getPermanentId(player1, "Fountain of Youth");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, fountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Helpers =====

    private Permanent addReadyJackal(Player player) {
        Permanent perm = new Permanent(new HurrJackal());
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addRegeneratingSkeleton(Player player) {
        Permanent perm = new Permanent(new DrudgeSkeletons());
        perm.setSummoningSick(false);
        perm.setRegenerationShield(1);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
