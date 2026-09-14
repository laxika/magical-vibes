package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HulkingOgre;
import com.github.laxika.magicalvibes.cards.s.SquirmingMass;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhyrexianMonitor.class, HulkingOgre.class, SquirmingMass.class})
class PhyrexianMonitorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the activated ability grants a regeneration shield")
    void regenerationAbilityGrantsShield() {
        Permanent monitor = addCreatureReady(player1, new PhyrexianMonitor());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(monitor.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating the ability consumes one black mana without tapping Phyrexian Monitor")
    void activationConsumesBlackManaWithoutTapping() {
        Permanent monitor = addCreatureReady(player1, new PhyrexianMonitor());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(monitor.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability requires black mana")
    void abilityRequiresBlackMana() {
        addCreatureReady(player1, new PhyrexianMonitor());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Multiple activations create multiple regeneration shields")
    void multipleActivationsStackShields() {
        Permanent monitor = addCreatureReady(player1, new PhyrexianMonitor());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(monitor.getRegenerationShield()).isEqualTo(2);
    }

    @Test
    @DisplayName("Regeneration shield saves Phyrexian Monitor from lethal combat damage")
    void regenerationSavesFromLethalCombat() {
        Permanent monitor = addCreatureReady(player1, new PhyrexianMonitor());
        monitor.setRegenerationShield(1);
        monitor.setBlocking(true);
        monitor.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new HulkingOgre());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Phyrexian Monitor");
        assertThat(monitor.isTapped()).isTrue();
        assertThat(monitor.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Regeneration shield is not spent by nonlethal combat damage")
    void nonlethalCombatDamageDoesNotSpendShield() {
        Permanent monitor = addCreatureReady(player1, new PhyrexianMonitor());
        monitor.setRegenerationShield(1);
        monitor.setBlocking(true);
        monitor.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new SquirmingMass());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Phyrexian Monitor");
        assertThat(monitor.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Phyrexian Monitor dies without a regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent monitor = addCreatureReady(player1, new PhyrexianMonitor());
        monitor.setBlocking(true);
        monitor.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new HulkingOgre());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Phyrexian Monitor");
        harness.assertInGraveyard(player1, "Phyrexian Monitor");
    }
}
