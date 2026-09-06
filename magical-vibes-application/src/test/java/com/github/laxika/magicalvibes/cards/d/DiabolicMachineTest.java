package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiabolicMachine.class, AirElemental.class})
class DiabolicMachineTest extends BaseCardTest {

    @Test
    @DisplayName("Activating regeneration ability puts it on the stack targeting the machine")
    void activatingAbilityPutsOnStack() {
        Permanent perm = addCreatureReady(player1, new DiabolicMachine());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(perm.getId());
    }

    @Test
    @DisplayName("Activating regeneration ability spends three generic mana without tapping Diabolic Machine")
    void activatingAbilityPaysManaWithoutTappingSource() {
        Permanent machine = addCreatureReady(player1, new DiabolicMachine());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(machine.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Diabolic Machine cannot activate regeneration without three generic mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new DiabolicMachine());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Resolving regeneration ability grants a regeneration shield")
    void resolvingAbilityGrantsRegenerationShield() {
        addCreatureReady(player1, new DiabolicMachine());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent machine = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(machine.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Diabolic Machine from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent machinePerm = addCreatureReady(player1, new DiabolicMachine());
        machinePerm.setRegenerationShield(1);
        machinePerm.setBlocking(true);
        machinePerm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new AirElemental());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Diabolic Machine");
        Permanent machine = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(machine.isTapped()).isTrue();
        assertThat(machine.getRegenerationShield()).isEqualTo(0);
        assertThat(machine.isBlocking()).isFalse();
        assertThat(machine.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Diabolic Machine dies in combat without regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent machinePerm = addCreatureReady(player1, new DiabolicMachine());
        machinePerm.setBlocking(true);
        machinePerm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new AirElemental());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Diabolic Machine");
        harness.assertInGraveyard(player1, "Diabolic Machine");
    }

}
