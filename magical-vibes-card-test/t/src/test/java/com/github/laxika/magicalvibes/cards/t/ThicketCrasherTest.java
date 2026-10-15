package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThicketCrasher.class, AirElemental.class, GreenwoodSentinel.class})
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
        Permanent sentinel = addCreatureReady(player1, new GreenwoodSentinel());

        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.TRAMPLE)).isFalse();
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

    @Test
    @DisplayName("Grants trample to an Elemental already on the battlefield")
    void grantsTrampleToExistingElemental() {
        Permanent elemental = addCreatureReady(player1, new AirElemental());
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.TRAMPLE)).isFalse();

        addCreatureReady(player1, new ThicketCrasher());

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Trample remains until the last Thicket Crasher leaves")
    void overlappingSourcesKeepGrantUntilBothLeave() {
        Permanent first = addCreatureReady(player1, new ThicketCrasher());
        Permanent second = addCreatureReady(player1, new ThicketCrasher());
        Permanent elemental = addCreatureReady(player1, new AirElemental());
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(first);

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(second);

        assertThat(gqs.hasKeyword(gd, elemental, Keyword.TRAMPLE)).isFalse();
    }
}
