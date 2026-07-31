package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BonescytheSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Bonescythe Sliver grants itself double strike (it is a Sliver)")
    void grantsSelfDoubleStrike() {
        Permanent sliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Grants double strike to another Sliver you control, and revokes it when it leaves")
    void grantsDoubleStrikeToOtherSliver() {
        Permanent sliver = addCreatureReady(player1, new BonescytheSliver());
        Permanent otherSliver = addCreatureReady(player1, new BonescytheSliver());

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(sliver);

        assertThat(gqs.hasKeyword(gd, otherSliver, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(otherSliver);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant double strike to a non-Sliver creature")
    void doesNotGrantToNonSliver() {
        addCreatureReady(player1, new BonescytheSliver());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not grant double strike to an opponent's non-Sliver creature")
    void doesNotGrantToOpponentCreature() {
        addCreatureReady(player1, new BonescytheSliver());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
