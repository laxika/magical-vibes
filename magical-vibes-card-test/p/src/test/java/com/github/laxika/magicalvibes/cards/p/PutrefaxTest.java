package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfTanglecord;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Putrefax.class, GrizzlyBears.class, WallOfTanglecord.class})
class PutrefaxTest extends BaseCardTest {

    @Test
    @DisplayName("Putrefax is sacrificed at end step")
    void sacrificedAtEndStep() {
        harness.addToBattlefield(player1, new Putrefax());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Putrefax");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Putrefax");
        harness.assertInGraveyard(player1, "Putrefax");
    }

    @Test
    @DisplayName("Putrefax can attack immediately due to haste")
    void canAttackImmediatelyDueToHaste() {
        harness.setLife(player2, 20);

        Permanent putrefax = harness.addToBattlefieldAndReturn(player1, new Putrefax());
        putrefax.setSummoningSick(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));

        // Life unchanged because infect deals poison, not life loss
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // 5 power = 5 poison counters
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(5);
    }

    @Test
    @DisplayName("Unblocked Putrefax deals 5 poison counters instead of life loss")
    void dealsPoisonCountersWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent putrefax = harness.addToBattlefieldAndReturn(player1, new Putrefax());
        putrefax.setSummoningSick(false);
        putrefax.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(5);
    }

    @Test
    @DisplayName("Blocked Putrefax deals -1/-1 counters to blocker")
    void dealsMinusCountersToBlocker() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);

        Permanent putrefax = harness.addToBattlefieldAndReturn(player1, new Putrefax());
        putrefax.setSummoningSick(false);
        putrefax.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(putrefax);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
        harness.passBothPriorities();

        // Putrefax has trample, so assign all 5 damage to blocker (no excess to player)
        harness.handleCombatDamageAssigned(player1, attackerIdx, Map.of(
                blocker.getId(), 5
        ));

        // Grizzly Bears (2/2) receives 5 -1/-1 counters → dies
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        // Putrefax (5/3) takes 2 regular damage from Grizzly Bears → survives
        harness.assertOnBattlefield(player1, "Putrefax");

        // No poison counters — all damage assigned to creature
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Trample with infect assigns excess damage as poison counters to defending player")
    void trampleInfectExcessDamageAsPoisonCounters() {
        harness.setLife(player2, 20);

        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);

        Permanent putrefax = harness.addToBattlefieldAndReturn(player1, new Putrefax());
        putrefax.setSummoningSick(false);
        putrefax.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // 5/3 trample+infect blocked by 2/2 → assign 2 lethal to blocker as -1/-1, 3 excess to player as poison
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 3
        ));

        // Grizzly Bears dies from -1/-1 counters
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        // Life unchanged — infect deals poison, not life loss
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // 3 excess damage dealt as poison counters
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
    }

    @Test
    @DisplayName("Putrefax is sacrificed during the opponent's end step")
    void sacrificedAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new Putrefax());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        harness.assertOnBattlefield(player1, "Putrefax");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Putrefax");
        harness.assertInGraveyard(player1, "Putrefax");
    }

    @Test
    @DisplayName("Infect leaves counters rather than marked damage on a surviving blocker")
    void infectCountersRemainOnSurvivingBlocker() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new WallOfTanglecord());
        Permanent putrefax = harness.addToBattlefieldAndReturn(player1, new Putrefax());
        putrefax.setSummoningSick(false);
        putrefax.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 5));

        harness.assertOnBattlefield(player2, "Wall of Tanglecord");
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(5);
        assertThat(blocker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Putrefax");
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }
}
