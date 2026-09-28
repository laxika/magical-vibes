package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AmbushViper;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WhiteWidowYelenaBelova.class, AmbushViper.class, GrizzlyBears.class})
class WhiteWidowYelenaBelovaTest extends BaseCardTest {

    @Test
    void deathtouchCreatureGetsCounterWhenItDealsCombatDamage() {
        Permanent whiteWidow = addReady(new WhiteWidowYelenaBelova());
        whiteWidow.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(whiteWidow.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void anotherDeathtouchCreatureGetsCounter() {
        addReady(new WhiteWidowYelenaBelova());
        Permanent viper = addReady(new AmbushViper());
        viper.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(viper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void creatureWithoutDeathtouchDoesNotGetCounter() {
        addReady(new WhiteWidowYelenaBelova());
        Permanent bears = addReady(new GrizzlyBears());
        bears.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addReady(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
