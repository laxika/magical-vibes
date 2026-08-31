package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DiabolicMachine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhostShip.class, DiabolicMachine.class})
class GhostShipTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {U}{U}{U} grants a regeneration shield")
    void payBlueGrantsRegenerationShield() {
        Permanent ship = addCreatureReady(player1, new GhostShip());
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ship.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate regeneration without enough blue mana")
    void cannotActivateWithoutEnoughBlueMana() {
        Permanent ship = addCreatureReady(player1, new GhostShip());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(ship.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Regeneration shield saves Ghost Ship from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent ship = addCreatureReady(player1, new GhostShip());
        ship.setRegenerationShield(1);
        ship.setBlocking(true);
        ship.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new DiabolicMachine());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Ghost Ship");
        assertThat(ship.isTapped()).isTrue();
        assertThat(ship.isBlocking()).isFalse();
        assertThat(ship.getMarkedDamage()).isZero();
        assertThat(ship.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ghost Ship dies in combat without a regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent ship = addCreatureReady(player1, new GhostShip());
        ship.setBlocking(true);
        ship.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new DiabolicMachine());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Ghost Ship");
        harness.assertInGraveyard(player1, "Ghost Ship");
    }
}
