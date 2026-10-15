package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OrcishArtillery;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ContestedWarZone.class, GrizzlyBears.class, OrcishArtillery.class})
class ContestedWarZoneTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Contested War Zone adds colorless mana without using the stack")
    void tappingAddsColorlessMana() {
        Permanent warZone = addContestedWarZone(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(warZone.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Unblocked attacker dealing combat damage to controller causes control change")
    void unblockedAttackerCausesControlChange() {
        addContestedWarZone(player2);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player1);
        resolveAllTriggers();

        // Contested War Zone should now be on player1's battlefield
        harness.assertOnBattlefield(player1, "Contested War Zone");
        harness.assertNotOnBattlefield(player2, "Contested War Zone");
    }

    @Test
    @DisplayName("Multiple attackers dealing combat damage leave the land with their controller")
    void multipleAttackersGainControl() {
        addContestedWarZone(player2);
        Permanent attacker1 = addCreatureReady(player1, new GrizzlyBears());
        attacker1.setAttacking(true);
        Permanent attacker2 = addCreatureReady(player1, new GrizzlyBears());
        attacker2.setAttacking(true);

        resolveCombat(player1);
        resolveAllTriggers();

        // Multiple triggers all give control to the same player.
        assertThat(countPermanents(player1, "Contested War Zone")).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Contested War Zone");
    }

    @Test
    @DisplayName("Ability damage does not trigger control change (combat only)")
    void abilityDamageDoesNotTriggerControlChange() {
        addContestedWarZone(player2);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new OrcishArtillery());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Contested War Zone should still be on player2's battlefield
        harness.assertOnBattlefield(player2, "Contested War Zone");
        harness.assertNotOnBattlefield(player1, "Contested War Zone");
    }

    @Test
    @DisplayName("Blocked attacker that deals no damage to player does not trigger control change")
    void blockedAttackerDoesNotTriggerControlChange() {
        addContestedWarZone(player2);

        // Small attacker that will die in combat
        GrizzlyBears smallAttacker = new GrizzlyBears();
        smallAttacker.setPower(1);
        smallAttacker.setToughness(1);
        Permanent attacker = addCreatureReady(player1, smallAttacker);
        attacker.setAttacking(true);

        // Big blocker that kills the attacker
        GrizzlyBears bigBlocker = new GrizzlyBears();
        bigBlocker.setPower(5);
        bigBlocker.setToughness(5);
        Permanent blocker = addCreatureReady(player2, bigBlocker);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player1);
        resolveAllTriggers();

        // Contested War Zone should still be on player2's battlefield (attacker was blocked and killed)
        harness.assertOnBattlefield(player2, "Contested War Zone");
    }

    @Test
    @DisplayName("Boost ability gives +1/+0 to attacking creatures")
    void boostAbilityGivesPlusOnePlusZero() {
        addContestedWarZone(player1);

        GrizzlyBears bearsCard = new GrizzlyBears();
        bearsCard.setPower(2);
        bearsCard.setToughness(2);
        Permanent attacker = addCreatureReady(player1, bearsCard);
        attacker.setAttacking(true);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        // Activate the second ability (index 1): {1}, {T}: Attacking creatures get +1/+0
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Attacker should have +1 power modifier
        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Boost ability does not boost non-attacking creatures")
    void boostAbilityDoesNotBoostNonAttackingCreatures() {
        addContestedWarZone(player1);

        GrizzlyBears bearsCard = new GrizzlyBears();
        bearsCard.setPower(2);
        bearsCard.setToughness(2);
        Permanent nonAttacker = addCreatureReady(player1, bearsCard);
        // Not attacking

        GrizzlyBears attackerCard = new GrizzlyBears();
        attackerCard.setPower(2);
        attackerCard.setToughness(2);
        Permanent attacker = addCreatureReady(player1, attackerCard);
        attacker.setAttacking(true);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Non-attacking creature should not be boosted
        assertThat(nonAttacker.getPowerModifier()).isEqualTo(0);
        // Attacking creature should be boosted
        assertThat(attacker.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage queues the control change and leaves time to respond")
    void controlChangeUsesTheStack() {
        gd.playerAutoStopSteps.put(player1.getId(), java.util.Set.of(TurnStep.COMBAT_DAMAGE));
        gd.playerAutoStopSteps.put(player2.getId(), java.util.Set.of(TurnStep.COMBAT_DAMAGE));
        Permanent warZone = addContestedWarZone(player2);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        resolveCombat(player1);

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player2, "Contested War Zone");
        harness.assertNotOnBattlefield(player1, "Contested War Zone");
        assertThat(gd.stack).hasSize(1);

        harness.passPriority(player1);
        harness.activateAbility(player2, 0, 0, null, null);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Contested War Zone");
        assertThat(warZone.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Each creature dealing combat damage creates a separate control-change trigger")
    void multipleAttackersCreateSeparateTriggers() {
        gd.playerAutoStopSteps.put(player1.getId(), java.util.Set.of(TurnStep.COMBAT_DAMAGE));
        gd.playerAutoStopSteps.put(player2.getId(), java.util.Set.of(TurnStep.COMBAT_DAMAGE));
        addContestedWarZone(player2);
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        resolveCombat(player1);

        harness.assertLife(player2, 16);
        assertThat(gd.stack).hasSize(2);
        harness.assertOnBattlefield(player2, "Contested War Zone");
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Contested War Zone");
    }

    @Test
    @DisplayName("The defending player can boost opposing attackers; the boost lasts until end of turn")
    void boostAffectsOpposingAttackersAndExpires() {
        Permanent warZone = addContestedWarZone(player2);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent laterAttacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, 1, null, null);
        assertThat(warZone.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isZero();
        assertThat(laterAttacker.getPowerModifier()).isZero();
        attacker.setAttacking(false);
        laterAttacker.setAttacking(true);
        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(laterAttacker.getPowerModifier()).isZero();

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(attacker.getPowerModifier()).isZero();
    }

    private Permanent addContestedWarZone(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ContestedWarZone());
    }
}
