package com.github.laxika.magicalvibes.cards.u;

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

@CardUsed(UthdenTroll.class)
class UthdenTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability with mana puts the regeneration ability on the stack")
    void activationStacksAbility() {
        Permanent troll = addCreatureReady(player1, new UthdenTroll());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(troll.getId());
    }

    @Test
    @DisplayName("Resolving the ability grants a regeneration shield")
    void resolvingGrantsRegenerationShield() {
        Permanent troll = addCreatureReady(player1, new UthdenTroll());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated regeneration shield saves Uthden Troll from lethal combat damage")
    void activatedRegenerationShieldSavesFromLethalCombatDamage() {
        Permanent troll = addCreatureReady(player1, new UthdenTroll());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        troll.setBlocking(true);
        troll.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new UthdenTroll());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Uthden Troll");
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getRegenerationShield()).isZero();
        assertThat(troll.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new UthdenTroll());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate the ability with only nonred mana")
    void cannotActivateWithOnlyNonRedMana() {
        addCreatureReady(player1, new UthdenTroll());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Activating the ability does not tap Uthden Troll")
    void activationDoesNotTapTroll() {
        Permanent troll = addCreatureReady(player1, new UthdenTroll());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(troll.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Regeneration shield saves Uthden Troll from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent troll = addCreatureReady(player1, new UthdenTroll());
        troll.setRegenerationShield(1);
        troll.setBlocking(true);
        troll.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new UthdenTroll());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Uthden Troll");
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Uthden Troll dies in lethal combat without a regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent troll = addCreatureReady(player1, new UthdenTroll());
        troll.setBlocking(true);
        troll.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new UthdenTroll());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Uthden Troll");
        harness.assertInGraveyard(player1, "Uthden Troll");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Troll can activate regeneration")
    void tappedSummoningSickTrollCanActivate() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new UthdenTroll());
        troll.setSummoningSick(true);
        troll.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.getRegenerationShield()).isEqualTo(1);
        assertThat(troll.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creating a shield does not regenerate the Troll immediately")
    void creatingShieldDoesNotHealOrTap() {
        Permanent troll = addCreatureReady(player1, new UthdenTroll());
        troll.setMarkedDamage(1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.getRegenerationShield()).isEqualTo(1);
        assertThat(troll.getMarkedDamage()).isEqualTo(1);
        assertThat(troll.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Multiple activations create separate shields and lethal damage spends only one")
    void multipleShieldsReplaceOneDestruction() {
        Permanent troll = addCreatureReady(player1, new UthdenTroll());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(troll.getRegenerationShield()).isEqualTo(2);

        troll.setBlocking(true);
        troll.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new UthdenTroll());
        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Uthden Troll");
        assertThat(troll.getRegenerationShield()).isEqualTo(1);
        assertThat(troll.getMarkedDamage()).isZero();
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.isBlocking()).isFalse();
        assertThat(troll.getBlockingTargets()).isEmpty();
    }
}
