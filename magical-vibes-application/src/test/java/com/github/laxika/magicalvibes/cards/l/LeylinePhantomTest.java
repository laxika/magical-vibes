package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.m.MahamotiDjinn;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
