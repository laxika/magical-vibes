package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

    private Permanent addReady(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
