package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CabarettiInitiate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FleetfootDancer.class, CabarettiInitiate.class})
class FleetfootDancerTest extends BaseCardTest {

    @Test
    @DisplayName("Haste allows Fleetfoot Dancer to attack the turn it enters")
    void hasteAllowsAttackingTheTurnItEnters() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FleetfootDancer()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Lifelink gains life from unblocked combat damage")
    void lifelinkGainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new FleetfootDancer());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to the defending player")
    void trampleAssignsExcessDamageToDefendingPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new FleetfootDancer());
        Permanent blocker = addCreatureReady(player2, new CabarettiInitiate());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Cabaretti Initiate");
    }

    @Test
    @DisplayName("Trample may assign all damage to a blocker and lifelink gains the full amount")
    void mayAssignAllDamageToBlocker() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new FleetfootDancer());
        Permanent blocker = addCreatureReady(player2, new CabarettiInitiate());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 4));

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Cabaretti Initiate");
        harness.assertOnBattlefield(player1, "Fleetfoot Dancer");
    }

    @Test
    @DisplayName("Both Dancers gain life when they deal lethal combat damage to each other")
    void lifelinkAppliesEvenWhenSourceDiesInCombat() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new FleetfootDancer());
        Permanent blocker = addCreatureReady(player2, new FleetfootDancer());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 4));

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 24);
        harness.assertInGraveyard(player1, "Fleetfoot Dancer");
        harness.assertInGraveyard(player2, "Fleetfoot Dancer");
        harness.assertNotOnBattlefield(player1, "Fleetfoot Dancer");
        harness.assertNotOnBattlefield(player2, "Fleetfoot Dancer");
    }
}
