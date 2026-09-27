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

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(8);
    }

    @Test
    @DisplayName("Half-life loss is rounded up")
    void halfLifeLossIsRoundedUp() {
        harness.setLife(player2, 23);
        Permanent radioactiveMan = addCreatureReady(player1, new RadioactiveMan());
        radioactiveMan.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(9);
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
}
