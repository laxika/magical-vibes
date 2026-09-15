package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CurseOfBloodletting;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({NoblePurpose.class, FreshVolunteers.class})
class NoblePurposeTest extends BaseCardTest {

    @Test
    @DisplayName("Controller gains life when a creature they control deals combat damage to a player")
    void gainsLifeOnCombatDamageToPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.addToBattlefield(player1, new NoblePurpose());
        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        // Player2 takes 2 combat damage; Player1 gains that much life.
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Life gain scales with the damage dealt")
    void lifeGainScalesWithDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.addToBattlefield(player1, new NoblePurpose());
        FreshVolunteers big = new FreshVolunteers();
        big.setPower(5);
        big.setToughness(5);
        Permanent attacker = addCreatureReady(player1, big);
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 15);
        harness.assertLife(player1, 25);
    }

    @Test
    @DisplayName("Fires on combat damage dealt to a blocking creature")
    void gainsLifeOnCombatDamageToBlocker() {
        harness.setLife(player1, 20);

        harness.addToBattlefield(player1, new NoblePurpose());
        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new FreshVolunteers());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        // Attacker dealt 2 to the blocker → controller gains 2 life.
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Only creatures the enchantment's controller controls trigger the life gain")
    void onlyOwnCreaturesTrigger() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Player1 owns Noble Purpose but does not attack.
        harness.addToBattlefield(player1, new NoblePurpose());

        // Player2's creature deals combat damage to player1 — should NOT gain player1 life.
        Permanent enemy = addCreatureReady(player2, new FreshVolunteers());
        enemy.setAttacking(true);

        resolveCombat(player2);

        harness.assertLife(player1, 18);
        // Player2 controls no Noble Purpose, so no life gain.
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each attacking creature triggers the life gain separately")
    void triggersPerCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.addToBattlefield(player1, new NoblePurpose());
        Permanent a = addCreatureReady(player1, new FreshVolunteers());
        a.setAttacking(true);
        Permanent b = addCreatureReady(player1, new FreshVolunteers());
        b.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        // Two 2/2s deal 4 total to player2; controller gains 4.
        harness.assertLife(player2, 16);
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Combat damage life gain waits for the triggered ability to resolve")
    void combatDamageWaitsForTriggeredLifeGain() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.addToBattlefield(player1, new NoblePurpose());
        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        attacker.setAttacking(true);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @CardUsed({NoblePurpose.class, FreshVolunteers.class, CurseOfBloodletting.class})
    @DisplayName("Life gain equals combat damage after damage multipliers")
    void lifeGainMatchesModifiedCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.addToBattlefield(player1, new NoblePurpose());
        Permanent curse = harness.addToBattlefieldAndReturn(player1, new CurseOfBloodletting());
        curse.setAttachedTo(player2.getId());

        Permanent attacker = addCreatureReady(player1, new FreshVolunteers());
        attacker.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 24);
    }

}
