package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvelineDeGrandpre.class, AmbushViper.class, GrizzlyBears.class})
class AvelineDeGrandpreTest extends BaseCardTest {

    @Test
    @DisplayName("A deathtouch creature gets counters equal to its combat damage")
    void deathtouchCreatureGetsCountersEqualToDamage() {
        harness.addToBattlefieldAndReturn(player1, new AvelineDeGrandpre());
        Permanent viper = addReady(new AmbushViper());
        viper.setAttacking(true);

        harness.setLife(player2, 20);
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(viper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature without deathtouch does not get counters")
    void nonDeathtouchCreatureDoesNotGetCounters() {
        harness.addToBattlefieldAndReturn(player1, new AvelineDeGrandpre());
        Permanent bears = addReady(new GrizzlyBears());
        bears.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Aveline gets counters equal to her own combat damage")
    void triggersForItself() {
        Permanent aveline = addReady(new AvelineDeGrandpre());
        aveline.setAttacking(true);

        harness.setLife(player2, 20);
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(aveline.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void faceDownAvelineDoesNotGrantCountersToDeathtouchCreature() {
        Permanent aveline = castAvelineFaceDown();
        Permanent viper = addReady(new AmbushViper());
        viper.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(aveline.isFaceDown()).isTrue();
        assertThat(viper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void turningFaceUpForDisguiseCostEnablesCombatTrigger() {
        Permanent aveline = castAvelineFaceDown();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aveline));
        assertThat(aveline.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        aveline.setSummoningSick(false);
        aveline.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(aveline.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void simultaneousDealersReceiveTheirOwnDamageAmount() {
        Permanent aveline = addReady(new AvelineDeGrandpre());
        Permanent viper = addReady(new AmbushViper());
        aveline.setAttacking(true);
        viper.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 15);
        assertThat(aveline.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(viper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void opponentDeathtouchCreatureDoesNotReceiveCounters() {
        harness.addToBattlefield(player1, new AvelineDeGrandpre());
        Permanent viper = harness.addToBattlefieldAndReturn(player2, new AmbushViper());
        viper.setSummoningSick(false);
        viper.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(viper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void removingAvelineAfterDamageDoesNotStopCountersOnDealer() {
        Permanent aveline = harness.addToBattlefieldAndReturn(player1, new AvelineDeGrandpre());
        Permanent viper = addReady(new AmbushViper());
        viper.setAttacking(true);
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(aveline);
        gd.playerGraveyards.get(player1.getId()).add(aveline.getOriginalCard());
        resolveAllTriggers();

        assertThat(viper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void removedDealerDoesNotRedirectCountersOntoAveline() {
        Permanent aveline = harness.addToBattlefieldAndReturn(player1, new AvelineDeGrandpre());
        Permanent viper = addReady(new AmbushViper());
        viper.setAttacking(true);
        harness.resolveCombatDamage();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(viper);
        gd.playerGraveyards.get(player1.getId()).add(viper.getOriginalCard());
        resolveAllTriggers();

        assertThat(aveline.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(viper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent castAvelineFaceDown() {
        harness.setHand(player1, List.of(new AvelineDeGrandpre()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Aveline de Grandpré");
    }

    private Permanent addReady(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
