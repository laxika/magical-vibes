package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ContagiousVorrac;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BiliousSkulldweller.class, ContagiousVorrac.class})
class BiliousSkulldwellerTest extends BaseCardTest {

    @Test
    @DisplayName("Unblocked combat damage gives the defending player a poison counter")
    void combatDamageGivesPoisonCounter() {
        harness.setLife(player2, 20);
        Permanent skulldweller = addCreatureReady(player1, new BiliousSkulldweller());
        skulldweller.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Deathtouch destroys a larger creature in combat")
    void deathtouchDestroysLargerCreature() {
        Permanent vorrac = harness.addToBattlefieldAndReturn(player1, new ContagiousVorrac());
        vorrac.setSummoningSick(false);
        vorrac.setAttacking(true);

        Permanent skulldweller = harness.addToBattlefieldAndReturn(player2, new BiliousSkulldweller());
        skulldweller.setSummoningSick(false);
        skulldweller.setBlocking(true);
        skulldweller.addBlockingTarget(0);

        resolveCombat();

        harness.assertInGraveyard(player1, "Contagious Vorrac");
        harness.assertInGraveyard(player2, "Bilious Skulldweller");
    }

    @Test
    @DisplayName("Toxic gives poison as damage is dealt without using the stack")
    void toxicDoesNotUseStack() {
        Permanent skulldweller = addCreatureReady(player1, new BiliousSkulldweller());
        skulldweller.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 19);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Increasing combat damage does not increase toxic's poison counters")
    void increasedDamageStillGivesOnePoisonCounter() {
        Permanent skulldweller = addCreatureReady(player1, new BiliousSkulldweller());
        skulldweller.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        skulldweller.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 17);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage to a blocking creature does not poison its controller")
    void blockedSkulldwellerDoesNotGivePoison() {
        Permanent skulldweller = addCreatureReady(player1, new BiliousSkulldweller());
        skulldweller.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new ContagiousVorrac());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.assertLife(player2, 20);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        harness.assertInGraveyard(player1, "Bilious Skulldweller");
        harness.assertInGraveyard(player2, "Contagious Vorrac");
    }
}
