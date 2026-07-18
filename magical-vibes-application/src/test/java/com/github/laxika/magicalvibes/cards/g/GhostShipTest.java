package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
    @DisplayName("Regeneration shield saves Ghost Ship from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent ship = addCreatureReady(player1, new GhostShip());
        ship.setRegenerationShield(1);
        ship.setBlocking(true);
        ship.addBlockingTarget(0);

        Permanent attacker = new Permanent(new com.github.laxika.magicalvibes.cards.a.AirElemental());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Ghost Ship");
        assertThat(ship.isTapped()).isTrue();
        assertThat(ship.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Ghost Ship dies in combat without a regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent ship = addCreatureReady(player1, new GhostShip());
        ship.setBlocking(true);
        ship.addBlockingTarget(0);

        Permanent attacker = new Permanent(new com.github.laxika.magicalvibes.cards.a.AirElemental());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ghost Ship");
        harness.assertInGraveyard(player1, "Ghost Ship");
    }
}
