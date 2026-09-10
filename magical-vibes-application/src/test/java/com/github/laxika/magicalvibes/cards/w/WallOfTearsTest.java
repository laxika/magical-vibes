package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CravenGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
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

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(attacker.getId()));
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

        harness.passBothPriorities();
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

        harness.passBothPriorities();
        harness.passBothPriorities();

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

        harness.passBothPriorities();
        harness.passBothPriorities();

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

        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(attacker.getId()));

        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Craven Giant");
    }
}
