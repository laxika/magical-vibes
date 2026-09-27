package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheFleshIsWeak.class, GrizzlyBears.class, Ornithopter.class})
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

    private void castTheFleshIsWeak() {
        harness.setHand(player1, List.of(new TheFleshIsWeak()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
