package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmpyrealVoyager.class, SuntailHawk.class})
class EmpyrealVoyagerTest extends BaseCardTest {

    @Test
    void getsEnergyEqualToCombatDamageDealtToPlayer() {
        Permanent voyager = addCreatureReady(player1, new EmpyrealVoyager());
        Permanent blocker = addCreatureReady(player2, new SuntailHawk());
        voyager.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 1
        ));
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void unblockedDamageAddsEnergyToExistingCountersOnlyAfterResolution() {
        Permanent voyager = addCreatureReady(player1, new EmpyrealVoyager());
        voyager.setAttacking(true);
        gd.playerEnergyCounters.put(player1.getId(), 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(3);
        resolveAllTriggers();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(5);
        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    void awardsEnergyToOtherPlayerWhenTheyControlVoyager() {
        Permanent voyager = addCreatureReady(player2, new EmpyrealVoyager());
        voyager.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void damageOnlyToBlockerDoesNotGrantEnergy() {
        Permanent voyager = addCreatureReady(player1, new EmpyrealVoyager());
        Permanent blocker = addCreatureReady(player2, new EmpyrealVoyager());
        voyager.setAttacking(true);
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 2));
        resolveAllTriggers();

        harness.assertLife(player2, 20);
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerEnergyCounters.getOrDefault(player2.getId(), 0)).isZero();
    }
}
