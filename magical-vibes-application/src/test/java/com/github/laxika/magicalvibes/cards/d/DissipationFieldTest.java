package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OrcishArtillery;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DissipationField.class, GrizzlyBears.class, OrcishArtillery.class})
class DissipationFieldTest extends BaseCardTest {

    // ===== Combat damage bounce =====

    @Test
    @DisplayName("Unblocked attacker dealing combat damage to controller is bounced to owner's hand")
    void unblockedAttackerIsBounced() {
        addDissipationField(player2);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        int defenderHandBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat(player1, player2);

        // Attacker should be bounced off the battlefield
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        // Attacker should be returned to owner's hand
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(defenderHandBefore + 1);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Multiple unblocked attackers each trigger a separate bounce")
    void multipleAttackersEachBounced() {
        addDissipationField(player2);
        Permanent attacker1 = addCreatureReady(player1, new GrizzlyBears());
        attacker1.setAttacking(true);
        Permanent attacker2 = addCreatureReady(player1, new GrizzlyBears());
        attacker2.setAttacking(true);

        resolveCombat(player1, player2);

        // Both attackers should be bounced
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    @DisplayName("Dissipation Field does not bounce creatures that dealt no damage (blocked and killed)")
    void blockedAttackerNotBounced() {
        addDissipationField(player2);

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

        resolveCombat(player1, player2);

        // Attacker should be dead (in graveyard), not bounced to hand
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    // ===== Spell/ability damage bounce =====

    @Test
    @DisplayName("Permanent dealing ability damage to controller triggers bounce")
    void abilityDamageTriggerssBounce() {
        addDissipationField(player2);
        harness.setLife(player2, 20);
        Permanent artillery = addCreatureReady(player1, new OrcishArtillery());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Orcish Artillery should be bounced to owner's hand
        harness.assertNotOnBattlefield(player1, "Orcish Artillery");
        harness.assertInHand(player1, "Orcish Artillery");
    }

    // ===== No trigger without Dissipation Field =====

    @Test
    @DisplayName("Without Dissipation Field, attacker is not bounced")
    void noBounceWithoutDissipationField() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player1, player2);

        // Attacker should still be on the battlefield
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    // ===== Dissipation Field itself is not bounced by combat damage =====

    @Test
    @DisplayName("Dissipation Field itself is not bounced (it's an enchantment, not the damage source)")
    void dissipationFieldNotBounced() {
        addDissipationField(player2);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player1, player2);

        // Dissipation Field should still be on the battlefield
        harness.assertOnBattlefield(player2, "Dissipation Field");
    }

    // ===== Helpers =====

    private void addDissipationField(Player player) {
        Permanent perm = new Permanent(new DissipationField());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
    }

    private void resolveCombat(Player attacker, Player defender) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
