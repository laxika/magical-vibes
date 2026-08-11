package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThicketCrasherTest extends BaseCardTest {

    @Test
    @DisplayName("Grants trample to other Elementals you control")
    void grantsTrampleToOtherElementalsYouControl() {
        addCreatureReady(player1, new ThicketCrasher());
        Permanent elemental = addCreatureReady(player1, new AirElemental());

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Does not grant trample to a non-Elemental creature")
    void doesNotGrantTrampleToNonElemental() {
        addCreatureReady(player1, new ThicketCrasher());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant trample to an opponent's Elemental")
    void doesNotGrantTrampleToOpponentsElemental() {
        addCreatureReady(player1, new ThicketCrasher());
        Permanent opponentElemental = addCreatureReady(player2, new AirElemental());

        assertThat(gqs.hasKeyword(gd, opponentElemental, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Granted trample is removed when Thicket Crasher leaves the battlefield")
    void removesGrantWhenSourceLeaves() {
        Permanent crasher = addCreatureReady(player1, new ThicketCrasher());
        Permanent elemental = addCreatureReady(player1, new AirElemental());

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(crasher);

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.TRAMPLE)).isFalse();
    }
}
