package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.m.MarshBoa;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Windscouter.class, MarshBoa.class})
class WindscouterTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking returns Windscouter to its owner's hand at end of combat")
    void attackingReturnsItToHand() {
        Permanent windscouter = addCreatureReady(player1, new Windscouter());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertNotOnBattlefield(player1, "Windscouter");
        harness.assertInHand(player1, "Windscouter");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(windscouter.getId()));
    }

    @Test
    @DisplayName("Blocking returns Windscouter to its owner's hand at end of combat")
    void blockingReturnsItToHand() {
        Permanent attacker = addCreatureReady(player1, new MarshBoa());
        attacker.setAttacking(true);
        addCreatureReady(player2, new Windscouter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertNotOnBattlefield(player2, "Windscouter");
        harness.assertInHand(player2, "Windscouter");
    }

    @Test
    @DisplayName("Windscouter is not returned if it leaves before end of combat")
    void notReturnedIfItLeavesBeforeEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new MarshBoa());
        attacker.setAttacking(true);
        Permanent windscouter = addCreatureReady(player2, new Windscouter());

        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
            resolveAllTriggers();
        });

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, windscouter));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertNotInHand(player2, "Windscouter");
        harness.assertInGraveyard(player2, "Windscouter");
    }

    @Test
    @DisplayName("Windscouter is not returned if it dies in combat")
    void notReturnedIfItDiesInCombat() {
        Permanent attacker = addCreatureReady(player1, new Windscouter());
        attacker.setAttacking(true);
        addCreatureReady(player2, new Windscouter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertInGraveyard(player2, "Windscouter");
        harness.assertNotInHand(player2, "Windscouter");
    }

    @Test
    @DisplayName("The end-of-combat return uses the stack after combat damage")
    void returnWaitsForEndOfCombatTriggerToResolve() {
        addCreatureReady(player1, new Windscouter());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player1, "Windscouter");
        harness.assertNotInHand(player1, "Windscouter");
        assertThat(gd.stack).hasSize(1);

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertNotOnBattlefield(player1, "Windscouter");
        harness.assertInHand(player1, "Windscouter");
    }

    @Test
    @DisplayName("A borrowed Windscouter returns to its owner rather than its controller")
    void returnsToOwnerHand() {
        Windscouter card = new Windscouter();
        card.setOwnerId(player2.getId());
        addCreatureReady(player1, card);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertNotOnBattlefield(player1, "Windscouter");
        harness.assertInHand(player2, "Windscouter");
        harness.assertNotInHand(player1, "Windscouter");
    }

    @Test
    @DisplayName("Leaving and returning creates a new permanent unaffected by the delayed return")
    void returnedPermanentIsNotBouncedAgain() {
        Permanent original = addCreatureReady(player1, new Windscouter());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToHand(gd, original));
        harness.assertInHand(player1, "Windscouter");
        gd.playerHands.get(player1.getId()).remove(original.getCard());
        Permanent returned = addCreatureReady(player1, original.getCard());

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        assertThat(findPermanent(player1, "Windscouter").getId()).isEqualTo(returned.getId());
        harness.assertNotInHand(player1, "Windscouter");
    }

    @Test
    @DisplayName("Changing control does not change the delayed trigger's controller")
    void delayedTriggerRetainsOriginalController() {
        Windscouter card = new Windscouter();
        card.setOwnerId(player1.getId());
        Permanent windscouter = addCreatureReady(player1, card);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        gd.playerBattlefields.get(player1.getId()).remove(windscouter);
        gd.playerBattlefields.get(player2.getId()).add(windscouter);
        windscouter.setAttacking(false);
        gd.stolenCreatures.put(windscouter.getId(), player1.getId());

        harness.passUntil(TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player2, "Windscouter");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());

        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);

        harness.assertInHand(player1, "Windscouter");
        harness.assertNotInHand(player2, "Windscouter");
    }

}
