package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RadioactiveMan.class, GrizzlyBears.class})
class RadioactiveManTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes the player lose half their life")
    void combatDamageMakesPlayerLoseHalfLife() {
        harness.setLife(player2, 22);
        Permanent radioactiveMan = addCreatureReady(player1, new RadioactiveMan());
        radioactiveMan.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(9);
    }

    @Test
    @DisplayName("Half-life loss is rounded up")
    void halfLifeLossIsRoundedUp() {
        harness.setLife(player2, 23);
        Permanent radioactiveMan = addCreatureReady(player1, new RadioactiveMan());
        radioactiveMan.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
    }

    @Test
    @DisplayName("No life loss occurs when no combat damage reaches a player")
    void noLifeLossWhenBlocked() {
        harness.setLife(player2, 22);
        Permanent radioactiveMan = addCreatureReady(player1, new RadioactiveMan());
        radioactiveMan.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Life loss uses the player's life total when the trigger resolves")
    void lifeLossUsesLifeAtResolution() {
        harness.setLife(player2, 22);
        Permanent radioactiveMan = addCreatureReady(player1, new RadioactiveMan());
        radioactiveMan.setAttacking(true);

        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, 19);
        harness.setLife(player2, 25);
        resolveAllTriggers();

        harness.assertLife(player2, 12);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The life-loss trigger resolves after Radioactive Man leaves the battlefield")
    void triggerResolvesWithoutSource() {
        harness.setLife(player2, 22);
        Permanent radioactiveMan = addCreatureReady(player1, new RadioactiveMan());
        radioactiveMan.setAttacking(true);

        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(radioactiveMan);
        gd.playerGraveyards.get(player1.getId()).add(radioactiveMan.getCard());
        resolveAllTriggers();

        harness.assertLife(player2, 9);
    }

    @Test
    @DisplayName("The damaged player loses life when the other player controls Radioactive Man")
    void otherControllerMakesDamagedPlayerLoseLife() {
        harness.setLife(player1, 23);
        Permanent radioactiveMan = addCreatureReady(player2, new RadioactiveMan());
        radioactiveMan.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
    }
}
