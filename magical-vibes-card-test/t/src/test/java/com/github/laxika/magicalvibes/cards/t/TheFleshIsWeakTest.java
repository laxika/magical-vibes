package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFleshIsWeak.class, GrizzlyBears.class, Ornithopter.class, Opalescence.class})
class TheFleshIsWeakTest extends BaseCardTest {

    @Test
    void entersWithCounterAndTurnsCounteredCreaturesIntoArtifacts() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castTheFleshIsWeak();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.isArtifact(gd, bears)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    void nonartifactCreaturesGetMinusOneMinusOneButArtifactsDoNot() {
        harness.addToBattlefield(player1, new TheFleshIsWeak());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        Permanent ornithopter = addCreatureReady(player2, new Ornithopter());

        assertThat(gqs.isArtifact(gd, bears)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ornithopter)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, ornithopter)).isEqualTo(2);
    }

    @Test
    void enterTriggerCountersAllControlledCreaturesIncludingArtifactsButNotOpponents() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castTheFleshIsWeak();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ornithopter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isArtifact(gd, opposingBears)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ornithopter)).isEqualTo(3);
    }

    @Test
    void artifactStatusAndPenaltyFollowCounterChanges() {
        harness.addToBattlefield(player1, new TheFleshIsWeak());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isArtifact(gd, bears)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);

        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.isArtifact(gd, bears)).isTrue();
        assertThat(gqs.isCreature(gd, bears)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        assertThat(gqs.isArtifact(gd, bears)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    void opposingCreatureWithCounterRemainsNonartifactAndGetsPenalty() {
        harness.addToBattlefield(player1, new TheFleshIsWeak());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.isArtifact(gd, bears)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void animatedSourceWithoutCounterGetsItsOwnPenalty() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new TheFleshIsWeak());
        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, enchantment)).isTrue();
        assertThat(gqs.isEnchantment(gd, enchantment)).isTrue();
        assertThat(gqs.isArtifact(gd, enchantment)).isFalse();
        assertThat(gqs.getEffectivePower(gd, enchantment)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, enchantment)).isEqualTo(4);

        enchantment.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        assertThat(gqs.isArtifact(gd, enchantment)).isTrue();
        assertThat(gqs.getEffectivePower(gd, enchantment)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, enchantment)).isEqualTo(6);
    }

    private void castTheFleshIsWeak() {
        harness.castFromHand(player1, new TheFleshIsWeak(), "{2}{W}{U}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
