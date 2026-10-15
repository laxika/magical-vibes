package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OrcishArtillery;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DissipationField.class, GrizzlyBears.class, OrcishArtillery.class})
class DissipationFieldTest extends BaseCardTest {

    @Test
    @DisplayName("Unblocked attacker dealing combat damage to controller is bounced to owner's hand")
    void unblockedAttackerIsBounced() {
        harness.addToBattlefield(player2, new DissipationField());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        int defenderHandBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat(player1);
        harness.passBothPriorities();

        // Attacker should be bounced off the battlefield
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        // Attacker should be returned to owner's hand
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(defenderHandBefore + 1);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Multiple unblocked attackers each trigger a separate bounce")
    void multipleAttackersEachBounced() {
        harness.addToBattlefield(player2, new DissipationField());
        Permanent attacker1 = addCreatureReady(player1, new GrizzlyBears());
        attacker1.setAttacking(true);
        Permanent attacker2 = addCreatureReady(player1, new GrizzlyBears());
        attacker2.setAttacking(true);

        resolveCombat(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Both attackers should be bounced
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Dissipation Field does not bounce creatures that dealt no damage (blocked and killed)")
    void blockedAttackerNotBounced() {
        harness.addToBattlefield(player2, new DissipationField());

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

        // Attacker should be dead (in graveyard), not bounced to hand
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Permanent dealing ability damage to controller triggers bounce")
    void abilityDamageTriggerssBounce() {
        harness.addToBattlefield(player2, new DissipationField());
        harness.setLife(player2, 20);
        addCreatureReady(player1, new OrcishArtillery());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Orcish Artillery should be bounced to owner's hand
        harness.assertNotOnBattlefield(player1, "Orcish Artillery");
        harness.assertInHand(player1, "Orcish Artillery");
    }

    @Test
    @DisplayName("Without Dissipation Field, attacker is not bounced")
    void noBounceWithoutDissipationField() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player1);

        // Attacker should still be on the battlefield
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Dissipation Field itself is not bounced (it's an enchantment, not the damage source)")
    void dissipationFieldNotBounced() {
        harness.addToBattlefield(player2, new DissipationField());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player1);
        harness.passBothPriorities();

        // Dissipation Field should still be on the battlefield
        harness.assertOnBattlefield(player2, "Dissipation Field");
    }

    @Test
    @DisplayName("Damage source stays on the battlefield until the triggered return resolves")
    void combatDamageReturnUsesTheStack() {
        harness.addToBattlefield(player2, new DissipationField());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.setLife(player2, 20);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Each Dissipation Field triggers even though they return the same permanent")
    void twoFieldsCreateSeparateTriggers() {
        harness.addToBattlefield(player2, new DissipationField());
        harness.addToBattlefield(player2, new DissipationField());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        assertThat(gd.stack).hasSize(2);
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Damage from your own permanent triggers Dissipation Field")
    void selfInflictedAbilityDamageTriggersReturn() {
        harness.addToBattlefield(player1, new DissipationField());
        addCreatureReady(player1, new OrcishArtillery());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Orcish Artillery");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Orcish Artillery");
        harness.assertInHand(player1, "Orcish Artillery");
    }
}
