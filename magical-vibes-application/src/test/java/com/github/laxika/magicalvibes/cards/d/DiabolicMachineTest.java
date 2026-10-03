package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiabolicMachine.class})
class DiabolicMachineTest extends BaseCardTest {

    @Test
    @DisplayName("Activating regeneration ability puts it on the stack referencing the machine")
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

        Permanent attacker = addCreatureReady(player2, new DiabolicMachine());
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

        Permanent attacker = addCreatureReady(player2, new DiabolicMachine());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Diabolic Machine");
        harness.assertInGraveyard(player1, "Diabolic Machine");
    }

    @Test
    @DisplayName("An activated regeneration shield is consumed by lethal combat damage")
    void activatedShieldSavesFromCombat() {
        Permanent machine = addCreatureReady(player1, new DiabolicMachine());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        machine.setBlocking(true);
        machine.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new DiabolicMachine());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Diabolic Machine");
        harness.assertNotInGraveyard(player1, "Diabolic Machine");
        assertThat(machine.getRegenerationShield()).isZero();
        assertThat(machine.getMarkedDamage()).isZero();
        assertThat(machine.isTapped()).isTrue();
        assertThat(machine.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Regeneration can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent machine = addCreatureReady(player1, new DiabolicMachine());
        machine.setSummoningSick(true);
        machine.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(machine.getRegenerationShield()).isEqualTo(1);
        assertThat(machine.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creating a regeneration shield does not tap the creature or remove existing damage")
    void creatingShieldDoesNotRegenerateImmediately() {
        Permanent machine = addCreatureReady(player1, new DiabolicMachine());
        machine.setMarkedDamage(2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(machine.getRegenerationShield()).isEqualTo(1);
        assertThat(machine.isTapped()).isFalse();
        assertThat(machine.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Repeated activations create independent regeneration shields")
    void repeatedActivationsCreateIndependentShields() {
        Permanent machine = addCreatureReady(player1, new DiabolicMachine());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(machine.getRegenerationShield()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(machine.isTapped()).isFalse();
    }

}
