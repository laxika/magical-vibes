package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Flutterfox;
import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({RagingRedcap.class, Flutterfox.class, Gingerbrute.class})
class RagingRedcapTest extends BaseCardTest {

    @Test
    @DisplayName("Double strike deals first-strike and regular combat damage")
    void doubleStrikeDealsBothCombatDamageSteps() {
        Permanent attacker = addCreatureReady(player1, new RagingRedcap());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new Flutterfox());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Raging Redcap");
        harness.assertInGraveyard(player2, "Flutterfox");
    }

    @Test
    @DisplayName("Unblocked double striker deals damage twice to the defending player")
    void unblockedDealsDamageTwice() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new RagingRedcap());
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Raging Redcap");
    }

    @Test
    @DisplayName("Killing the blocker in first-strike damage does not deal regular damage to the player")
    void killingBlockerDoesNotMakeAttackerUnblocked() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new RagingRedcap());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Gingerbrute());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertOnBattlefield(player1, "Raging Redcap");
        harness.assertInGraveyard(player2, "Gingerbrute");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Double strike also deals damage in both steps while blocking")
    void blockingDealsDamageTwice() {
        Permanent attacker = addCreatureReady(player1, new Flutterfox());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RagingRedcap());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Flutterfox");
        harness.assertInGraveyard(player2, "Raging Redcap");
    }
}
