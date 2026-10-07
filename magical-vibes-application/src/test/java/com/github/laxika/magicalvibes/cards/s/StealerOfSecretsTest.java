package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StealerOfSecrets.class, SerraAngel.class})
class StealerOfSecretsTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when dealing combat damage to a player")
    void drawsOnCombatDamageToPlayer() {
        Permanent stealer = addCreatureReady(player1, new StealerOfSecrets());
        stealer.setAttacking(true);
        harness.setLife(player2, 20);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Does not draw when blocked and no combat damage reaches a player")
    void noDrawWhenBlocked() {
        Permanent stealer = addCreatureReady(player1, new StealerOfSecrets());
        stealer.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("The attacking creature's controller draws, even when player two attacks")
    void opposingControllerDraws() {
        Permanent stealer = addCreatureReady(player2, new StealerOfSecrets());
        stealer.setAttacking(true);
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
    @DisplayName("The draw trigger resolves after Stealer of Secrets leaves the battlefield")
    void drawsAfterSourceLeavesBattlefield() {
        Permanent stealer = addCreatureReady(player1, new StealerOfSecrets());
        stealer.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        gd.playerBattlefields.get(player1.getId()).remove(stealer);
        gd.playerGraveyards.get(player1.getId()).add(stealer.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.stack).isEmpty();
    }
}
