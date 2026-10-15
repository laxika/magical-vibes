package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

@CardUsed({DuskdaleWurm.class, RuneclawBear.class})
class DuskdaleWurmTest extends BaseCardTest {

    @Test
    void dealsExcessDamageThroughOneBlocker() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DuskdaleWurm());
        Permanent blocker = addCreatureReady(player2, new RuneclawBear());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 5));

        harness.assertLife(player2, 15);
        harness.assertOnBattlefield(player1, "Duskdale Wurm");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }

    @Test
    void dealsExcessDamageAfterAssigningLethalToEveryBlocker() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DuskdaleWurm());
        Permanent first = addCreatureReady(player2, new RuneclawBear());
        Permanent second = addCreatureReady(player2, new RuneclawBear());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(first.getId(), 2, second.getId(), 2, player2.getId(), 3));

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player1, "Duskdale Wurm");
        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    void cannotTrampleOverWhenBlockersAbsorbAllDamage() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DuskdaleWurm());
        Permanent first = addCreatureReady(player2, new RuneclawBear());
        Permanent second = addCreatureReady(player2, new RuneclawBear());
        Permanent third = addCreatureReady(player2, new RuneclawBear());
        Permanent fourth = addCreatureReady(player2, new RuneclawBear());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0), new BlockerAssignment(3, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                first.getId(), 2, second.getId(), 2, third.getId(), 2, fourth.getId(), 1));

        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Duskdale Wurm");
        harness.assertInGraveyard(player1, "Duskdale Wurm");
        harness.assertOnBattlefield(player2, "Runeclaw Bear");
    }

    @Test
    void canAssignAllDamageToBlockerInsteadOfTramplingOver() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new DuskdaleWurm());
        Permanent blocker = addCreatureReady(player2, new RuneclawBear());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 7));

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Duskdale Wurm");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
    }
}
