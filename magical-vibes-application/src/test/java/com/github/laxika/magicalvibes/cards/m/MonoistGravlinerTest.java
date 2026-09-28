package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MonoistGravliner.class, GrizzlyBears.class})
class MonoistGravlinerTest extends BaseCardTest {

    @Test
    @DisplayName("Stationing a creature perpetually gives it deathtouch and lifelink")
    void stationingCreatureGainsKeywords() {
        Permanent gravliner = harness.addToBattlefieldAndReturn(player1, new MonoistGravliner());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(gravliner.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Six charge counters animate Monoist Gravliner and grant its keywords")
    void sixChargeCountersUnlockAbilities() {
        Permanent gravliner = harness.addToBattlefieldAndReturn(player1, new MonoistGravliner());

        gravliner.setCounterCount(CounterType.CHARGE, 5);
        assertThat(gqs.isCreature(gd, gravliner)).isFalse();
        assertThat(gqs.hasKeyword(gd, gravliner, Keyword.FLYING)).isFalse();

        gravliner.setCounterCount(CounterType.CHARGE, 6);
        assertThat(gqs.isCreature(gd, gravliner)).isTrue();
        assertThat(gqs.hasKeyword(gd, gravliner, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, gravliner, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, gravliner, Keyword.LIFELINK)).isTrue();
    }
}
