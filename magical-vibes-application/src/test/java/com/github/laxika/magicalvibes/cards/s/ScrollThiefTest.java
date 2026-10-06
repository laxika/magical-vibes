package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScrollThief.class, SerraAngel.class})
class ScrollThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when dealing combat damage to a player")
    void drawsCardOnCombatDamage() {
        Permanent thief = addCreatureReady(player1, new ScrollThief());
        thief.setAttacking(true);
        harness.setLife(player2, 20);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        // Player2 takes 1 combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Player1 should have drawn a card
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("No trigger when Scroll Thief is blocked and killed")
    void noTriggerWhenBlocked() {
        Permanent thief = addCreatureReady(player1, new ScrollThief());
        thief.setAttacking(true);

        // 4/4 blocker kills the 1/3 Scroll Thief
        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        // Scroll Thief should be dead
        harness.assertInGraveyard(player1, "Scroll Thief");

        // No card drawn (didn't deal damage to a player)
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("Combat damage draws for Scroll Thief's controller, not the damaged player")
    void drawsForOtherController() {
        Permanent thief = addCreatureReady(player2, new ScrollThief());
        thief.setAttacking(true);
        int controllerHandBefore = gd.playerHands.get(player2.getId()).size();
        int defenderHandBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat(player2);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(controllerHandBefore);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(controllerHandBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(defenderHandBefore);
    }

    @Test
    @DisplayName("The draw trigger resolves after Scroll Thief leaves the battlefield")
    void drawsAfterSourceLeavesBattlefield() {
        Permanent thief = addCreatureReady(player1, new ScrollThief());
        thief.setAttacking(true);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(thief);
        gd.playerGraveyards.get(player1.getId()).add(thief.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }
}
