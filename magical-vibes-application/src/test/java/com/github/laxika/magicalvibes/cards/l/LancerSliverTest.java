package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LancerSliver.class, MetallicSliver.class, GrizzlyBears.class})
class LancerSliverTest extends BaseCardTest {

    @Test
    void grantsFirstStrikeToItselfAndOtherSliversYouControl() {
        Permanent lancer = addCreatureReady(player1, new LancerSliver());
        Permanent otherSliver = addCreatureReady(player1, new MetallicSliver());

        assertThat(gqs.hasKeyword(gd, lancer, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void doesNotGrantFirstStrikeToNonSliversOrOpponentsSlivers() {
        addCreatureReady(player1, new LancerSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentSliver = addCreatureReady(player2, new MetallicSliver());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.FIRST_STRIKE)).isFalse();
    }
}
