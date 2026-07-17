package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClayStatueTest extends BaseCardTest {

    // ===== Regeneration ability =====

    @Test
    @DisplayName("Activating regeneration ability puts it on the stack targeting the statue")
    void activatingAbilityPutsOnStack() {
        Permanent perm = addStatueReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(perm.getId());
    }

    @Test
    @DisplayName("Resolving regeneration ability grants a regeneration shield")
    void resolvingAbilityGrantsRegenerationShield() {
        addStatueReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent statue = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(statue.getRegenerationShield()).isEqualTo(1);
    }

    // ===== Regeneration saves from lethal combat damage =====

    @Test
    @DisplayName("Regeneration shield saves Clay Statue from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent statuePerm = addStatueReady(player1);
        statuePerm.setRegenerationShield(1);
        statuePerm.setBlocking(true);
        statuePerm.addBlockingTarget(0);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Clay Statue");
        Permanent statue = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(statue.isTapped()).isTrue();
        assertThat(statue.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Clay Statue dies in combat without regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent statuePerm = addStatueReady(player1);
        statuePerm.setBlocking(true);
        statuePerm.addBlockingTarget(0);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Clay Statue");
        harness.assertInGraveyard(player1, "Clay Statue");
    }

    // ===== Helper methods =====

    private Permanent addStatueReady(Player player) {
        Permanent perm = new Permanent(new ClayStatue());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
