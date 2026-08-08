package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.q.QasaliAmbusher;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MazeBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Grants trample to a multicolored creature you control, and revokes it when it leaves")
    void grantsTrampleToOwnMulticoloredCreature() {
        Permanent behemoth = addCreatureReady(player1, new MazeBehemoth());
        Permanent multicolored = addCreatureReady(player1, new QasaliAmbusher()); // {1}{G}{W}

        assertThat(gqs.hasKeyword(gd, multicolored, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(behemoth);

        assertThat(gqs.hasKeyword(gd, multicolored, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant trample to a monocolored creature you control")
    void doesNotGrantToMonocoloredCreature() {
        addCreatureReady(player1, new MazeBehemoth());
        Permanent monocolored = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, monocolored, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant trample to an opponent's multicolored creature")
    void doesNotGrantToOpponentMulticoloredCreature() {
        addCreatureReady(player1, new MazeBehemoth());
        Permanent opponentMulticolored = addCreatureReady(player2, new QasaliAmbusher());

        assertThat(gqs.hasKeyword(gd, opponentMulticolored, Keyword.TRAMPLE)).isFalse();
    }

}
