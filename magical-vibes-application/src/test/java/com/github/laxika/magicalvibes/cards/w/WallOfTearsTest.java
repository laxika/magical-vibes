package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CravenGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WallOfTears.class, CravenGiant.class})
class WallOfTearsTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking a creature schedules that attacker for an end-of-combat bounce")
    void blockingSchedulesReturnToHand() {
        Permanent attacker = addCreatureReady(player1, new CravenGiant());
        attacker.setAttacking(true);
        addCreatureReady(player2, new WallOfTears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Wall of Tears")
                        && se.getTargetId().equals(attacker.getId()));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, this::resolveAllTriggers);
        harness.assertOnBattlefield(player1, "Craven Giant");
        harness.assertNotInHand(player1, "Craven Giant");
        harness.passUntil(TurnStep.END_OF_COMBAT);
        resolveAllTriggers();
        harness.assertInHand(player1, "Craven Giant");
    }

    @Test
    @DisplayName("The block ability still resolves after Wall of Tears leaves the battlefield")
    void triggerResolvesAfterWallLeavesBattlefield() {
        Permanent attacker = addCreatureReady(player1, new CravenGiant());
        attacker.setAttacking(true);
        Permanent wall = addCreatureReady(player2, new WallOfTears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(wall.getId()));

        harness.passUntil(TurnStep.END_OF_COMBAT);
        resolveAllTriggers();
        harness.assertInHand(player1, "Craven Giant");
    }

    @Test
    @DisplayName("The blocked attacker is returned to its owner's hand at end of combat")
    void blockedAttackerReturnedToHand() {
        Permanent attacker = addCreatureReady(player1, new CravenGiant());
        attacker.setAttacking(true);
        addCreatureReady(player2, new WallOfTears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passUntil(TurnStep.END_OF_COMBAT);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Craven Giant");
        harness.assertInHand(player1, "Craven Giant");
    }

    @Test
    @DisplayName("The blocked attacker still deals combat damage before the bounce")
    void attackerStillDealsCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new CravenGiant());
        attacker.setAttacking(true);
        Permanent wall = addCreatureReady(player2, new WallOfTears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passUntil(TurnStep.END_OF_COMBAT);
        resolveAllTriggers();

        assertThat(wall.getMarkedDamage()).isEqualTo(4);
        harness.assertInHand(player1, "Craven Giant");
    }

    @Test
    @DisplayName("An attacker that left the battlefield before end of combat is not returned")
    void attackerGoneBeforeEndOfCombatIsNotReturned() {
        Permanent attacker = addCreatureReady(player1, new CravenGiant());
        attacker.setAttacking(true);
        addCreatureReady(player2, new WallOfTears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, this::resolveAllTriggers);
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(attacker.getId()));

        harness.passUntil(TurnStep.END_OF_COMBAT);
        resolveAllTriggers();

        harness.assertNotInHand(player1, "Craven Giant");
    }

    @Test
    @DisplayName("The end-of-combat return uses the stack and allows a response")
    void returnWaitsForDelayedTriggerToResolve() {
        Permanent attacker = addCreatureReady(player1, new CravenGiant());
        attacker.setAttacking(true);
        addCreatureReady(player2, new WallOfTears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player1, "Craven Giant");
        harness.assertNotInHand(player1, "Craven Giant");
        assertThat(gd.stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && attacker.getId().equals(entry.getTargetId()));

        resolveAllTriggers();
        harness.assertInHand(player1, "Craven Giant");
        harness.assertInGraveyard(player2, "Wall of Tears");
    }

    @Test
    @DisplayName("A creature that leaves and returns is not the creature tracked by the delayed ability")
    void returnedCreatureIsANewObject() {
        Permanent attacker = addCreatureReady(player1, new CravenGiant());
        attacker.setAttacking(true);
        addCreatureReady(player2, new WallOfTears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, this::resolveAllTriggers);

        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        Permanent returned = addCreatureReady(player1, attacker.getCard());
        harness.passUntil(TurnStep.END_OF_COMBAT);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(returned);
        harness.assertNotInHand(player1, "Craven Giant");
    }
}
