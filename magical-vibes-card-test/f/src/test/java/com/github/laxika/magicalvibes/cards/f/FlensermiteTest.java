package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.o.OgreResister;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({Flensermite.class, OgreResister.class})
class FlensermiteTest extends BaseCardTest {

    @Test
    @DisplayName("Unblocked Flensermite deals poison counters to defending player")
    void unblockedDealsPoisonCounters() {
        Permanent flensermite = addCreatureReady(player1, new Flensermite());
        flensermite.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
        // Life should NOT change from infect damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Flensermite deals -1/-1 counters to blocking creature")
    void dealsMinusCountersToBlocker() {
        Permanent flensermite = addCreatureReady(player1, new Flensermite());
        flensermite.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new OgreResister());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Ogre Resister survives the counter from Flensermite.
        Permanent ogre = findPermanent(player2, "Ogre Resister");
        assertThat(ogre.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Unblocked Flensermite with lifelink gains controller life equal to poison damage")
    void lifelinkGainsLifeOnPoisonDamage() {
        Permanent flensermite = addCreatureReady(player1, new Flensermite());
        flensermite.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();

        // Lifelink gains life even when infect gives poison counters.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        // Defender gets poison, not life loss
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Flensermite with lifelink gains controller life when dealing -1/-1 counters to blocker")
    void lifelinkGainsLifeOnCreatureDamage() {
        Permanent flensermite = addCreatureReady(player1, new Flensermite());
        flensermite.setAttacking(true);
        harness.setLife(player1, 20);

        Permanent blocker = addCreatureReady(player2, new OgreResister());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Lifelink still works when dealing -1/-1 counters to creatures
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Blocking Flensermite gives the attacking creature a counter and gains its controller life")
    void infectAndLifelinkWorkWhileBlocking() {
        Permanent attacker = addCreatureReady(player1, new OgreResister());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Flensermite());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();

        assertThat(attacker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Ogre Resister");
        harness.assertInGraveyard(player2, "Flensermite");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 21);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Opposing Flensermites both die to infect and both controllers gain life")
    void simultaneousInfectDamageStillGrantsBothPlayersLifelink() {
        Permanent attacker = addCreatureReady(player1, new Flensermite());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Flensermite());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();

        harness.assertNotOnBattlefield(player1, "Flensermite");
        harness.assertNotOnBattlefield(player2, "Flensermite");
        harness.assertInGraveyard(player1, "Flensermite");
        harness.assertInGraveyard(player2, "Flensermite");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 21);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("The tenth poison counter wins the game while lifelink still gains life")
    void lethalPoisonDamageAlsoGrantsLifelink() {
        Permanent attacker = addCreatureReady(player1, new Flensermite());
        attacker.setAttacking(true);
        gd.playerPoisonCounters.put(player2.getId(), 9);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(10);
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }
}