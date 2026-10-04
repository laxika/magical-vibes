package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AmbushViper;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FynnTheFangbearer.class, AmbushViper.class, GrizzlyBears.class})
class FynnTheFangbearerTest extends BaseCardTest {

    @Test
    @DisplayName("A deathtouch creature dealing combat damage gives two poison counters")
    void deathtouchCreatureGivesTwoPoisonCounters() {
        addCreatureReady(player1, new FynnTheFangbearer()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each deathtouch creature dealing combat damage triggers separately")
    void eachDeathtouchCreatureTriggersSeparately() {
        addCreatureReady(player1, new FynnTheFangbearer()).setAttacking(true);
        addCreatureReady(player1, new AmbushViper()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(4);
    }

    @Test
    @DisplayName("Poison applies to the damaged player without targeting through shroud")
    void damagedPlayerWithShroudStillGetsPoison() {
        gd.playersWithShroudThisTurn.add(player2.getId());
        addCreatureReady(player1, new FynnTheFangbearer()).setAttacking(true);

        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        resolveAllTriggers();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature without deathtouch does not trigger Fynn")
    void nonDeathtouchCreatureDoesNotTrigger() {
        addCreatureReady(player1, new FynnTheFangbearer());
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);

        resolveCombat();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("A blocked deathtouch creature does not trigger Fynn")
    void blockedDeathtouchCreatureDoesNotTrigger() {
        Permanent fynn = addCreatureReady(player1, new FynnTheFangbearer());
        fynn.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("An opponent's deathtouch creature does not trigger Fynn")
    void opposingDeathtouchCreatureDoesNotTrigger() {
        addCreatureReady(player1, new FynnTheFangbearer());
        addCreatureReady(player2, new AmbushViper()).setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Prevented combat damage does not trigger Fynn")
    void preventedCombatDamageDoesNotTrigger() {
        addCreatureReady(player1, new FynnTheFangbearer()).setAttacking(true);
        gd.preventAllCombatDamage = true;

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Poison trigger resolves after Fynn leaves the battlefield")
    void triggerResolvesWithoutFynn() {
        Permanent fynn = addCreatureReady(player1, new FynnTheFangbearer());
        fynn.setAttacking(true);

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(fynn);
        gd.playerGraveyards.get(player1.getId()).add(fynn.getCard());
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }
}
