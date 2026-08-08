package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RakdosShredFreak;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MazeGliderTest extends BaseCardTest {

    @Test
    @DisplayName("Grants flying to a multicolored creature you control, and revokes it when the Glider leaves")
    void grantsFlyingToOwnMulticoloredCreature() {
        Permanent glider = addCreatureReady(player1, new MazeGlider());
        Permanent gold = addCreatureReady(player1, new RakdosShredFreak());

        assertThat(gqs.hasKeyword(gd, gold, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(glider);

        assertThat(gqs.hasKeyword(gd, gold, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not grant flying to a monocolored creature you control")
    void doesNotGrantToMonocoloredCreature() {
        addCreatureReady(player1, new MazeGlider());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not grant flying to an opponent's multicolored creature")
    void doesNotGrantToOpponentMulticoloredCreature() {
        addCreatureReady(player1, new MazeGlider());
        Permanent opponentGold = addCreatureReady(player2, new RakdosShredFreak());

        assertThat(gqs.hasKeyword(gd, opponentGold, Keyword.FLYING)).isFalse();
    }
}
