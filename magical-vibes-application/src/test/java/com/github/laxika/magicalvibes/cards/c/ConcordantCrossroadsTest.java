package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConcordantCrossroadsTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures have haste")
    void grantsHasteToAllCreatures() {
        harness.addToBattlefield(player1, new ConcordantCrossroads());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Grizzly Bears"), Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering after Concordant Crossroads also have haste")
    void grantsHasteToLaterCreatures() {
        harness.addToBattlefield(player1, new ConcordantCrossroads());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Creatures lose the granted haste when Concordant Crossroads leaves")
    void hasteEndsWhenSourceLeaves() {
        harness.addToBattlefield(player1, new ConcordantCrossroads());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        gd.playerBattlefields.get(player1.getId()).removeIf(
                permanent -> permanent.getCard().getName().equals("Concordant Crossroads"));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isFalse();
    }
}
