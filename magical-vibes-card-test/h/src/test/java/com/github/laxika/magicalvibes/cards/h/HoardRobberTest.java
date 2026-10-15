package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DireWolfProwler;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HoardRobber.class, DireWolfProwler.class})
class HoardRobberTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure token when it deals combat damage to a player")
    void createsTreasureTokenOnCombatDamageToPlayer() {
        Permanent robber = addReadyHoardRobber();
        robber.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create a Treasure token when blocked")
    void doesNotCreateTreasureTokenWhenBlocked() {
        Permanent robber = addReadyHoardRobber();
        robber.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new DireWolfProwler());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Each Hoard Robber creates its own Treasure when both deal combat damage")
    void createsTreasureForEachRobber() {
        addReadyHoardRobber().setAttacking(true);
        addReadyHoardRobber().setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Creates the Treasure for the attacking Robber's controller")
    void createsTreasureForOpponentWhenOpponentAttacks() {
        addCreatureReady(player2, new HoardRobber()).setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Prevented combat damage does not create a Treasure")
    void doesNotTriggerWhenCombatDamageIsPrevented() {
        addReadyHoardRobber().setAttacking(true);
        gd.preventAllCombatDamageToPlayers = true;

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Treasure creation uses the stack and survives removal of the Robber")
    void createsTreasureAfterSourceLeavesBattlefield() {
        Permanent robber = addReadyHoardRobber();
        robber.setAttacking(true);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 19);
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, robber));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Hoard Robber");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanent(player1, "Treasure").isTapped()).isFalse();
    }

    private Permanent addReadyHoardRobber() {
        return addCreatureReady(player1, new HoardRobber());
    }
}
