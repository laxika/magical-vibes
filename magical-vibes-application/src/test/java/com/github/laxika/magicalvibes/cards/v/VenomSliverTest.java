package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VenomSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Venom Sliver grants itself deathtouch (it is a Sliver)")
    void grantsSelfDeathtouch() {
        Permanent sliver = addCreatureReady(player1, new VenomSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Grants deathtouch to another Sliver you control")
    void grantsDeathtouchToOtherSliver() {
        addCreatureReady(player1, new VenomSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("Deathtouch is revoked when Venom Sliver leaves the battlefield")
    void revokesWhenSourceLeaves() {
        Permanent sliver = addCreatureReady(player1, new VenomSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        gd.playerBattlefields.get(player1.getId()).remove(sliver);

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Does not grant deathtouch to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new VenomSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Does not grant deathtouch to an opponent's Sliver")
    void doesNotGrantToOpponentSliver() {
        addCreatureReady(player1, new VenomSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.DEATHTOUCH)).isFalse();
    }
}
