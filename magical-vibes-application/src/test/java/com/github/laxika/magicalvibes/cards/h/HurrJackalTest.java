package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HurrJackal.class, DrudgeSkeletons.class, Forest.class, GrizzlyBears.class})
class HurrJackalTest extends BaseCardTest {

    // ===== Activation =====

    @Test
    @DisplayName("Activating the ability puts it on the stack targeting the creature and taps Hurr Jackal")
    void activatingPutsAbilityOnStack() {
        Permanent jackal = addCreatureReady(player1, new HurrJackal());
        Permanent skele = addRegeneratingSkeleton(player2);

        harness.activateAbility(player1, 0, null, skele.getId());

        assertThat(jackal.isTapped()).isTrue();
        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(skele.getId());
    }

    // ===== Resolution marks the creature =====

    @Test
    @DisplayName("Resolving marks the target so it can't be regenerated, leaving its shield intact")
    void resolvingMarksTargetCantBeRegenerated() {
        addCreatureReady(player1, new HurrJackal());
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
        addCreatureReady(player1, new HurrJackal());
        Permanent skele = addRegeneratingSkeleton(player2);

        harness.activateAbility(player1, 0, null, skele.getId());
        harness.passBothPriorities();

        // Now the marked skeleton blocks a lethal attacker.
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        skele.setBlocking(true);
        skele.addBlockingTargetId(bears.getId());

        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Drudge Skeletons");
        harness.assertInGraveyard(player2, "Drudge Skeletons");
    }

    @Test
    @DisplayName("Without Hurr Jackal's mark, the same creature regenerates and survives combat")
    void withoutMarkCreatureRegeneratesInCombat() {
        Permanent skele = addRegeneratingSkeleton(player2);

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        skele.setBlocking(true);
        skele.addBlockingTargetId(bears.getId());

        resolveCombat();

        harness.assertOnBattlefield(player2, "Drudge Skeletons");
        assertThat(skele.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("A creature can activate its regeneration ability while it can't be regenerated")
    void canActivateRegenerationWhileMarked() {
        addCreatureReady(player1, new HurrJackal());
        Permanent skele = addCreatureReady(player2, new DrudgeSkeletons());

        harness.activateAbility(player1, 0, null, skele.getId());
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(skele.getRegenerationShield()).isEqualTo(1);
    }

    // ===== Mark wears off =====

    @Test
    @DisplayName("The can't-be-regenerated mark clears during end-of-turn cleanup")
    void markClearsAtEndOfTurn() {
        addCreatureReady(player1, new HurrJackal());
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
        addCreatureReady(player1, new HurrJackal());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Helpers =====

    private Permanent addRegeneratingSkeleton(Player player) {
        Permanent perm = addCreatureReady(player, new DrudgeSkeletons());
        perm.setRegenerationShield(1);
        return perm;
    }
}
