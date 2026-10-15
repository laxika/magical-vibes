package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VampireOfTheDireMoon.class, CentaurCourser.class})
class VampireOfTheDireMoonTest extends BaseCardTest {

    @Test
    @DisplayName("Deathtouch destroys a larger creature in combat")
    void deathtouchDestroysLargerCreature() {
        Permanent vampire = addCreatureReady(player1, new VampireOfTheDireMoon());
        vampire.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new CentaurCourser());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(vampire);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    @DisplayName("Lifelink gains life when it deals combat damage")
    void lifelinkGainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent vampire = addCreatureReady(player1, new VampireOfTheDireMoon());
        vampire.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Blocking Vampire gains life even when both creatures die")
    void blockingGainsLifeDespiteLethalDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new CentaurCourser());
        attacker.setAttacking(true);
        Permanent vampire = addCreatureReady(player2, new VampireOfTheDireMoon());
        vampire.setBlocking(true);
        vampire.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(vampire);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Zero power deals no damage and causes neither deathtouch nor lifelink")
    void zeroPowerDoesNotDestroyBlockerOrGainLife() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent vampire = addCreatureReady(player1, new VampireOfTheDireMoon());
        vampire.setPowerModifier(-1);
        vampire.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new CentaurCourser());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(vampire);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}