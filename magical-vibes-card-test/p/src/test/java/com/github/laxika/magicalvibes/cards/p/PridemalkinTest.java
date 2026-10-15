package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Pridemalkin.class, GrizzlyBears.class})
class PridemalkinTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a +1/+1 counter on a target creature you control")
    void entersAndPutsCounterOnControlledCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castFromHand(player1, new Pridemalkin(), "{2}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures you control with +1/+1 counters have trample")
    void counteredControlledCreaturesHaveTrample() {
        harness.addToBattlefield(player1, new Pridemalkin());
        Permanent counteredCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent uncounteredCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, counteredCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, uncounteredCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Opponents' countered creatures do not have trample from Pridemalkin")
    void opponentCreaturesDoNotGainTrample() {
        harness.addToBattlefield(player1, new Pridemalkin());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("A creature loses trample when its +1/+1 counter is removed")
    void trampleEndsWhenCounterIsRemoved() {
        harness.addToBattlefield(player1, new Pridemalkin());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Pridemalkin can target itself and gains trample from its counter")
    void canPutCounterOnItself() {
        harness.castFromHand(player1, new Pridemalkin(), "{2}{G}");
        harness.passBothPriorities();
        Permanent pridemalkin = gqs.findPermanentById(gd,
                harness.getPermanentId(player1, "Pridemalkin"));
        harness.handlePermanentChosen(player1, pridemalkin.getId());
        harness.passBothPriorities();

        assertThat(pridemalkin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, pridemalkin, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Trample ends when Pridemalkin leaves the battlefield")
    void trampleEndsWhenSourceLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Pridemalkin());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(source);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Other counter types do not grant trample")
    void otherCountersDoNotGrantTrample() {
        Permanent pridemalkin = harness.addToBattlefieldAndReturn(player1, new Pridemalkin());
        pridemalkin.setCounterCount(CounterType.CHARGE, 1);

        assertThat(gqs.hasKeyword(gd, pridemalkin, Keyword.TRAMPLE)).isFalse();
    }
}
