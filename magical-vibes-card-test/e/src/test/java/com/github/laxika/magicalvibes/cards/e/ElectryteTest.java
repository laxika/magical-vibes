package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.Cathodion;
import com.github.laxika.magicalvibes.cards.w.WallOfJunk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Electryte.class, Cathodion.class, WallOfJunk.class})
class ElectryteTest extends BaseCardTest {

    @Test
    @DisplayName("Deals its current power to each blocking creature when it deals combat damage to a player")
    void dealsCurrentPowerToBlockingCreatures() {
        harness.setLife(player2, 20);
        Permanent electryte = addCreatureReady(player1, new Electryte());
        electryte.setPowerModifier(2);
        electryte.setAttacking(true);
        Permanent attacker = addAttackingCreature(player1, new Cathodion());
        attacker.setPowerModifier(-3);
        attacker.setToughnessModifier(2);
        Permanent blocker = addBlockingCreature(player2, new WallOfJunk(), 1);
        Permanent bystander = addCreatureReady(player2, new WallOfJunk());

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(blocker.getMarkedDamage()).isEqualTo(5);
        assertThat(bystander.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Deals its current power to every blocking creature")
    void dealsCurrentPowerToEveryBlockingCreature() {
        harness.setLife(player2, 20);
        Permanent electryte = addCreatureReady(player1, new Electryte());
        electryte.setPowerModifier(2);
        electryte.setAttacking(true);
        Permanent attacker = addAttackingCreature(player1, new Cathodion());
        attacker.setPowerModifier(-3);
        attacker.setToughnessModifier(2);
        Permanent firstBlocker = addBlockingCreature(player2, new WallOfJunk(), 1);
        Permanent secondBlocker = addBlockingCreature(player2, new WallOfJunk(), 1);

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        assertThat(firstBlocker.getMarkedDamage()).isEqualTo(5);
        assertThat(secondBlocker.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not trigger when Electryte is blocked")
    void doesNotTriggerWhenBlocked() {
        harness.setLife(player2, 20);
        Permanent electryte = addCreatureReady(player1, new Electryte());
        electryte.setPowerModifier(2);
        electryte.setAttacking(true);
        Permanent blocker = addBlockingCreature(player2, new WallOfJunk(), 0);

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(blocker.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Uses power at resolution rather than the combat damage dealt")
    void usesPowerAtResolution() {
        Permanent electryte = addAttackingCreature(player1, new Electryte());
        Permanent attacker = addAttackingCreature(player1, new Cathodion());
        attacker.setPowerModifier(-3);
        Permanent blocker = addBlockingCreature(player2, new WallOfJunk(), 1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(blocker.getMarkedDamage()).isZero();
        electryte.setPowerModifier(2);
        resolveAllTriggers();

        assertThat(blocker.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Only damages creatures still blocking when the trigger resolves")
    void checksBlockingStatusAtResolution() {
        addAttackingCreature(player1, new Electryte());
        Permanent attacker = addAttackingCreature(player1, new Cathodion());
        attacker.setPowerModifier(-3);
        Permanent blocker = addBlockingCreature(player2, new WallOfJunk(), 1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        blocker.setBlocking(false);
        resolveAllTriggers();

        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not trigger when its combat damage is zero")
    void doesNotTriggerForZeroCombatDamage() {
        Permanent electryte = addAttackingCreature(player1, new Electryte());
        electryte.setPowerModifier(-3);
        Permanent attacker = addAttackingCreature(player1, new Cathodion());
        attacker.setPowerModifier(-3);
        Permanent blocker = addBlockingCreature(player2, new WallOfJunk(), 1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        assertThat(gd.stack).isEmpty();
        electryte.setPowerModifier(2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Uses last known power if Electryte dies before its trigger resolves")
    void usesLastKnownPowerAfterSourceDies() {
        Permanent electryte = addAttackingCreature(player1, new Electryte());
        Permanent attacker = addAttackingCreature(player1, new Cathodion());
        attacker.setPowerModifier(-3);
        Permanent blocker = addBlockingCreature(player2, new WallOfJunk(), 1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        electryte.setPowerModifier(2);
        electryte.setToughnessModifier(-3);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Electryte");
        resolveAllTriggers();

        assertThat(blocker.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Deals no damage if its power becomes zero before resolution")
    void dealsNoDamageWithZeroPowerAtResolution() {
        Permanent electryte = addAttackingCreature(player1, new Electryte());
        Permanent attacker = addAttackingCreature(player1, new Cathodion());
        attacker.setPowerModifier(-3);
        Permanent blocker = addBlockingCreature(player2, new WallOfJunk(), 1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        electryte.setPowerModifier(-3);
        resolveAllTriggers();

        assertThat(blocker.getMarkedDamage()).isZero();
    }

    private Permanent addAttackingCreature(Player player, Card card) {
        Permanent creature = addCreatureReady(player, card);
        creature.setAttacking(true);
        return creature;
    }

    private Permanent addBlockingCreature(Player player, Card card, int attackerIndex) {
        Permanent creature = addCreatureReady(player, card);
        creature.setBlocking(true);
        creature.addBlockingTarget(attackerIndex);
        return creature;
    }
}
