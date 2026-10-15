package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnderdarkBasilisk.class, HillGiant.class})
class UnderdarkBasiliskTest extends BaseCardTest {

    @Test
    @DisplayName("Deathtouch destroys a larger creature in combat")
    void deathtouchDestroysLargerCreature() {
        Permanent hillGiant = addCreatureReady(player1, new HillGiant());
        hillGiant.setAttacking(true);

        Permanent basilisk = addCreatureReady(player2, new UnderdarkBasilisk());
        basilisk.setBlocking(true);
        basilisk.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hillGiant);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(basilisk);
    }

    @Test
    @DisplayName("Attacking basilisk kills a blocker despite dealing only one damage")
    void attackingBasiliskKillsBlocker() {
        Permanent attacker = addCreatureReady(player1, new UnderdarkBasilisk());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new UnderdarkBasilisk());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(attacker.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
    }

    @Test
    @DisplayName("Zero power does not cause a deathtouch kill")
    void zeroPowerDoesNotKillBlocker() {
        Permanent attacker = addCreatureReady(player1, new UnderdarkBasilisk());
        attacker.setPowerModifier(-1);
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new UnderdarkBasilisk());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(blocker.getCard());
    }

    @Test
    @DisplayName("Deathtouch deals ordinary combat damage to a player")
    void unblockedBasiliskDealsOneDamageToPlayer() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new UnderdarkBasilisk());
        attacker.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }
}
