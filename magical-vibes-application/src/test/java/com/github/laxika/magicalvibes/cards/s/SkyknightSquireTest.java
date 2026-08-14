package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkyknightSquireTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when another creature you control enters")
    void putsCounterWhenAnotherCreatureEnters() {
        Permanent squire = addCreatureReady(player1, new SkyknightSquire());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(squire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when Skyknight Squire itself enters")
    void doesNotTriggerOnItsOwnEntry() {
        harness.setHand(player1, List.of(new SkyknightSquire()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent squire = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(squire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("With three +1/+1 counters, has flying and is a Knight")
    void gainsFlyingAndKnightTypeAtThreeCounters() {
        Permanent squire = addCreatureReady(player1, new SkyknightSquire());

        assertThat(gqs.hasKeyword(gd, squire, Keyword.FLYING)).isFalse();
        assertThat(gqs.computeStaticBonus(gd, squire).grantedSubtypes()).doesNotContain(CardSubtype.KNIGHT);

        squire.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        assertThat(gqs.hasKeyword(gd, squire, Keyword.FLYING)).isTrue();
        assertThat(gqs.computeStaticBonus(gd, squire).grantedSubtypes()).contains(CardSubtype.KNIGHT);

        squire.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThat(gqs.hasKeyword(gd, squire, Keyword.FLYING)).isFalse();
        assertThat(gqs.computeStaticBonus(gd, squire).grantedSubtypes()).doesNotContain(CardSubtype.KNIGHT);
    }
}
