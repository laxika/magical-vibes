package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SoulknifeSpy.class)
class SoulknifeSpyTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when it deals combat damage to a player")
    void drawsOnCombatDamageToPlayer() {
        Permanent spy = addCreatureReady(player1, new SoulknifeSpy());
        spy.setAttacking(true);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Does not draw when blocked")
    void doesNotDrawWhenBlocked() {
        Permanent spy = addCreatureReady(player1, new SoulknifeSpy());
        spy.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new SoulknifeSpy());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("Each unblocked Spy draws one card for its controller")
    void eachSpyDrawsOneCard() {
        addCreatureReady(player1, new SoulknifeSpy()).setAttacking(true);
        addCreatureReady(player1, new SoulknifeSpy()).setAttacking(true);
        int attackingHandSize = gd.playerHands.get(player1.getId()).size();
        int defendingHandSize = gd.playerHands.get(player2.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(attackingHandSize + 2);
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(defendingHandSize);
    }

    @Test
    @DisplayName("Draw waits for the trigger to resolve and survives the Spy leaving play")
    void drawTriggerSurvivesSourceLeavingBattlefield() {
        Permanent spy = addCreatureReady(player1, new SoulknifeSpy());
        spy.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(spy);
        gd.playerGraveyards.get(player1.getId()).add(spy.getCard());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }
}
