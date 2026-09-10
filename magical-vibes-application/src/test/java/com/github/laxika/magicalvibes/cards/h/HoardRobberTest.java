package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HoardRobber.class, GrizzlyBears.class})
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
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    private Permanent addReadyHoardRobber() {
        return addCreatureReady(player1, new HoardRobber());
    }
}
