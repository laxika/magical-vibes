package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
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

class DiabolicMachineTest extends BaseCardTest {

    // ===== Regeneration ability =====

    @Test
    @DisplayName("Activating regeneration ability puts it on the stack targeting the machine")
    void activatingAbilityPutsOnStack() {
        Permanent perm = addMachineReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(perm.getId());
    }

    @Test
    @DisplayName("Resolving regeneration ability grants a regeneration shield")
    void resolvingAbilityGrantsRegenerationShield() {
        addMachineReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent machine = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(machine.getRegenerationShield()).isEqualTo(1);
    }

    // ===== Regeneration saves from lethal combat damage =====

    @Test
    @DisplayName("Regeneration shield saves Diabolic Machine from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent machinePerm = addMachineReady(player1);
        machinePerm.setRegenerationShield(1);
        machinePerm.setBlocking(true);
        machinePerm.addBlockingTarget(0);

        Permanent attacker = new Permanent(new AirElemental());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Diabolic Machine");
        Permanent machine = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(machine.isTapped()).isTrue();
        assertThat(machine.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Diabolic Machine dies in combat without regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent machinePerm = addMachineReady(player1);
        machinePerm.setBlocking(true);
        machinePerm.addBlockingTarget(0);

        Permanent attacker = new Permanent(new AirElemental());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Diabolic Machine");
        harness.assertInGraveyard(player1, "Diabolic Machine");
    }

    // ===== Helper methods =====

    private Permanent addMachineReady(Player player) {
        Permanent perm = new Permanent(new DiabolicMachine());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
