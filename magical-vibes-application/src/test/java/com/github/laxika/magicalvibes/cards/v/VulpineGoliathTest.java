package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

@CardUsed({VulpineGoliath.class, TravelingPhilosopher.class})
class VulpineGoliathTest extends BaseCardTest {

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleAssignsExcessCombatDamageToDefendingPlayer() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new VulpineGoliath());
        attacker.setAttacking(true);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 4
        ));

        harness.assertNotOnBattlefield(player2, "Traveling Philosopher");
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Trample requires lethal damage to every blocker before player damage")
    void trampleOverMultipleBlockers() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new VulpineGoliath());
        attacker.setAttacking(true);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        first.setBlocking(true);
        first.addBlockingTarget(0);
        second.setBlocking(true);
        second.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                first.getId(), 2, second.getId(), 2, player2.getId(), 2));

        harness.assertNotOnBattlefield(player2, "Traveling Philosopher");
        harness.assertOnBattlefield(player1, "Vulpine Goliath");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Trample accounts for damage already marked on the blocker")
    void trampleAccountsForMarkedDamage() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new VulpineGoliath());
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        blocker.setMarkedDamage(1);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1, player2.getId(), 5));

        harness.assertNotOnBattlefield(player2, "Traveling Philosopher");
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Trample allows all damage to be assigned to the blocker")
    void trampleDoesNotRequirePlayerDamage() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new VulpineGoliath());
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new TravelingPhilosopher());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 6));

        harness.assertNotOnBattlefield(player2, "Traveling Philosopher");
        harness.assertLife(player2, 20);
    }
}
