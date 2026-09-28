package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DefenderOfTheQueue.class, GrizzlyBears.class})
class DefenderOfTheQueueTest extends BaseCardTest {

    @Test
    void boostsCreaturesImmediatelyToItsLeftAndRight() {
        Permanent left = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new DefenderOfTheQueue());
        Permanent right = addCreatureReady(player1, new GrizzlyBears());
        Permanent beyondRight = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, left)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, left)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, left, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, right)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, right)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, right, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, beyondRight)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, beyondRight, Keyword.VIGILANCE)).isFalse();
    }
}
