package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GiantDustwasp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AetherMembrane.class, GiantDustwasp.class})
class AetherMembraneTest extends BaseCardTest {

    @Test
    @DisplayName("The end-of-combat return uses the stack before moving the attacker")
    void endOfCombatReturnAllowsResponses() {
        addCreatureReady(player1, new GiantDustwasp());
        addCreatureReady(player2, new AetherMembrane());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player1, "Giant Dustwasp");
        harness.assertNotInHand(player1, "Giant Dustwasp");
        assertThat(gd.stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Giant Dustwasp");
        harness.assertInHand(player1, "Giant Dustwasp");
    }

    @Test
    @DisplayName("A blocked creature returns to its owner rather than its controller")
    void borrowedAttackerReturnsToOwner() {
        GiantDustwasp dustwasp = new GiantDustwasp();
        dustwasp.setOwnerId(player2.getId());
        addCreatureReady(player1, dustwasp);
        addCreatureReady(player2, new AetherMembrane());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertNotOnBattlefield(player1, "Giant Dustwasp");
        harness.assertNotInHand(player1, "Giant Dustwasp");
        harness.assertInHand(player2, "Giant Dustwasp");
    }

    @Test
    @DisplayName("Blocking a creature schedules that attacker for an end-of-combat bounce")
    void blockingSchedulesReturnToHand() {
        Permanent attacker = addCreatureReady(player1, new GiantDustwasp());
        addCreatureReady(player2, new AetherMembrane());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Aether Membrane")
                        && se.getTargetId().equals(attacker.getId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .anyMatch(a -> a.permanentId().equals(attacker.getId()));
    }

    @Test
    @DisplayName("The blocked attacker is returned to its owner's hand at end of combat")
    void blockedAttackerReturnedToHand() {
        addCreatureReady(player1, new GiantDustwasp());
        addCreatureReady(player2, new AetherMembrane());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Giant Dustwasp");
        harness.assertInHand(player1, "Giant Dustwasp");
    }

    @Test
    @DisplayName("The blocked attacker still deals combat damage before the bounce")
    void attackerStillDealsCombatDamage() {
        addCreatureReady(player1, new GiantDustwasp());
        Permanent membrane = addCreatureReady(player2, new AetherMembrane());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(membrane.getMarkedDamage()).isEqualTo(3);
        harness.assertInHand(player1, "Giant Dustwasp");
    }

    @Test
    @DisplayName("An attacker that left the battlefield before end of combat is not returned")
    void attackerGoneBeforeEndOfCombatIsNotReturned() {
        Permanent attacker = addCreatureReady(player1, new GiantDustwasp());
        addCreatureReady(player2, new AetherMembrane());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(attacker.getId()));

        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Giant Dustwasp");
    }

    @Test
    @DisplayName("The block ability still resolves after Aether Membrane leaves the battlefield")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        addCreatureReady(player1, new GiantDustwasp());
        Permanent membrane = addCreatureReady(player2, new AetherMembrane());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(membrane.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Giant Dustwasp");
    }
}
