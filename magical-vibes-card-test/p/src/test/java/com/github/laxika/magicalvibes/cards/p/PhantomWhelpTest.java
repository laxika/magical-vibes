package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DruidLyrist;
import com.github.laxika.magicalvibes.cards.r.RayOfCommand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhantomWhelp.class, DruidLyrist.class, RayOfCommand.class})
class PhantomWhelpTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking returns Phantom Whelp to its owner's hand at end of combat")
    void attackingReturnsItToHand() {
        harness.setLife(player2, 20);
        Permanent whelp = addCreatureReady(player1, new PhantomWhelp());

        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player1, "Phantom Whelp");
        harness.assertInHand(player1, "Phantom Whelp");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(whelp.getId()));
    }

    @Test
    @DisplayName("Blocking returns Phantom Whelp to its owner's hand at end of combat")
    void blockingReturnsItToHand() {
        Permanent attacker = addCreatureReady(player1, new DruidLyrist());
        attacker.setAttacking(true);
        addCreatureReady(player2, new PhantomWhelp());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.assertNotOnBattlefield(player2, "Phantom Whelp");
        harness.assertInHand(player2, "Phantom Whelp");
    }

    @Test
    @DisplayName("Phantom Whelp is not returned if it leaves before end of combat")
    void notReturnedIfItLeavesBeforeEndOfCombat() {
        Permanent attacker = addCreatureReady(player1, new DruidLyrist());
        attacker.setAttacking(true);
        Permanent whelp = addCreatureReady(player2, new PhantomWhelp());

        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
            resolveAllTriggers();
        });

        gd.playerBattlefields.get(player2.getId()).remove(whelp);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.assertNotInHand(player2, "Phantom Whelp");
    }

    @Test
    @DisplayName("Phantom Whelp is not returned if it dies in combat")
    void notReturnedIfItDiesInCombat() {
        Permanent attacker = addCreatureReady(player1, new PhantomWhelp());
        attacker.setAttacking(true);
        addCreatureReady(player2, new PhantomWhelp());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.assertInGraveyard(player2, "Phantom Whelp");
        harness.assertNotInHand(player2, "Phantom Whelp");
    }

    @Test
    @DisplayName("End-of-combat return waits for its delayed trigger to resolve")
    void returnWaitsForDelayedTriggerResolution() {
        addCreatureReady(player1, new PhantomWhelp());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player1, "Phantom Whelp");
        harness.assertNotInHand(player1, "Phantom Whelp");

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.assertNotOnBattlefield(player1, "Phantom Whelp");
        harness.assertInHand(player1, "Phantom Whelp");
    }

    @Test
    @DisplayName("A Whelp that does not attack or block remains on the battlefield")
    void idleWhelpIsNotReturned() {
        addCreatureReady(player1, new DruidLyrist());
        addCreatureReady(player1, new PhantomWhelp());

        declareAttackers(List.of(0));
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        harness.assertOnBattlefield(player1, "Phantom Whelp");
        harness.assertNotInHand(player1, "Phantom Whelp");
    }

    @Test
    @DisplayName("Changing control does not change the controller of the delayed return")
    void delayedReturnKeepsOriginalAbilityController() {
        Permanent whelp = addCreatureReady(player1, new PhantomWhelp());
        harness.setHand(player2, List.of(new RayOfCommand()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            harness.castAndResolveInstant(player2, 0, whelp.getId());
        });
        harness.assertOnBattlefield(player2, "Phantom Whelp");

        harness.passUntil(player1, TurnStep.END_OF_COMBAT);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
    }
}
