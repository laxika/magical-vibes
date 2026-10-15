package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.m.MahamotiDjinn;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({LeylinePhantom.class, GrizzlyBears.class, MahamotiDjinn.class})
class LeylinePhantomTest extends BaseCardTest {

    @Test
    @DisplayName("Returns to its owner's hand after dealing unblocked combat damage")
    void returnsToHandAfterDealingCombatDamageToPlayer() {
        Permanent phantom = addCreatureReady(player1, new LeylinePhantom());
        phantom.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Leyline Phantom");
        harness.assertNotInGraveyard(player1, "Leyline Phantom");
    }

    @Test
    @DisplayName("Returns to its owner's hand after surviving combat with a blocker")
    void returnsToHandAfterDealingCombatDamageToCreature() {
        Permanent phantom = addCreatureReady(player1, new LeylinePhantom());
        phantom.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Leyline Phantom");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not return when it dies in combat")
    void doesNotReturnWhenItDiesInCombat() {
        Permanent phantom = addCreatureReady(player1, new LeylinePhantom());
        phantom.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new MahamotiDjinn());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Leyline Phantom");
        harness.assertNotInHand(player1, "Leyline Phantom");
    }
    @Test
    @DisplayName("Returns after dealing combat damage while blocking")
    void returnsToHandAfterBlocking() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        Permanent phantom = addCreatureReady(player2, new LeylinePhantom());
        phantom.setBlocking(true);
        phantom.addBlockingTarget(0);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Leyline Phantom");
        harness.assertNotOnBattlefield(player2, "Leyline Phantom");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Returns to its owner rather than its controller")
    void returnsToOwnerWhenControlledByOpponent() {
        LeylinePhantom card = new LeylinePhantom();
        card.setOwnerId(player2.getId());
        Permanent phantom = addCreatureReady(player1, card);
        phantom.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Leyline Phantom");
        harness.assertNotInHand(player1, "Leyline Phantom");
        harness.assertNotOnBattlefield(player1, "Leyline Phantom");
    }

    @Test
    @DisplayName("Only the copy that deals combat damage returns")
    void leavesNonattackingCopyOnBattlefield() {
        Permanent attacker = addCreatureReady(player1, new LeylinePhantom());
        attacker.setAttacking(true);
        addCreatureReady(player1, new LeylinePhantom());

        resolveCombat();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Leyline Phantom");
        org.assertj.core.api.Assertions.assertThat(findPermanents(player1, "Leyline Phantom"))
                .hasSize(1)
                .doesNotContain(attacker);
    }
}
