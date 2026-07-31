package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SyphonSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Syphon Sliver grants itself lifelink (it is a Sliver)")
    void grantsSelfLifelink() {
        Permanent sliver = addCreatureReady(player1, new SyphonSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Grants lifelink to another Sliver you control")
    void grantsLifelinkToOtherSliver() {
        addCreatureReady(player1, new SyphonSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Lifelink is revoked when Syphon Sliver leaves the battlefield")
    void revokesLifelinkWhenGone() {
        Permanent sliver = addCreatureReady(player1, new SyphonSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        gd.playerBattlefields.get(player1.getId()).remove(sliver);

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Does not grant lifelink to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new SyphonSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Does not grant lifelink to an opponent's Sliver")
    void doesNotGrantToOpponentSliver() {
        addCreatureReady(player1, new SyphonSliver());
        Permanent opponentSliver = addCreatureReady(player2, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, opponentSliver, Keyword.LIFELINK)).isFalse();
    }
}
