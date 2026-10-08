package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TerritorialScythecat.class, Forest.class})
class TerritorialScythecatTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall waits for resolution and repeated land entries accumulate permanent counters")
    void landEntriesAccumulateCounters() {
        Permanent scythecat = harness.addToBattlefieldAndReturn(player1, new TerritorialScythecat());

        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gd.stack).hasSize(1);
        assertThat(scythecat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        assertThat(scythecat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(scythecat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Scythecat puts the counter on itself and nonlands do not trigger landfall")
    void multipleScythecatsTriggerIndependently() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new TerritorialScythecat());
        Permanent second = harness.enterBattlefieldAndReturn(player1, new TerritorialScythecat());

        assertThat(gd.stack).isEmpty();

        harness.enterBattlefieldAndReturn(player1, new Forest());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Trample deals excess combat damage through a blocker")
    void trampleDealsExcessCombatDamage() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new TerritorialScythecat());
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new TerritorialScythecat());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 1, player2.getId(), 2));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Territorial Scythecat");
        harness.assertInGraveyard(player1, "Territorial Scythecat");
    }

    @Test
    @DisplayName("Landfall puts a +1/+1 counter on Territorial Scythecat")
    void landfallPutsCounterOnSelf() {
        Permanent scythecat = harness.addToBattlefieldAndReturn(player1, new TerritorialScythecat());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(scythecat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's land does not trigger Territorial Scythecat")
    void opponentLandDoesNotTrigger() {
        Permanent scythecat = harness.addToBattlefieldAndReturn(player1, new TerritorialScythecat());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(scythecat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
