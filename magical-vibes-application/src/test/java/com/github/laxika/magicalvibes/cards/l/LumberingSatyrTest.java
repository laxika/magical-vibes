package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LumberingSatyrTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures, including Lumbering Satyr, have forestwalk")
    void grantsForestwalkToAllCreaturesIncludingItself() {
        harness.addToBattlefield(player1, new LumberingSatyr());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Lumbering Satyr"), Keyword.FORESTWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.FORESTWALK)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Grizzly Bears"), Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("A creature entering later also has forestwalk")
    void grantsForestwalkToCreaturesEnteringLater() {
        harness.addToBattlefield(player1, new LumberingSatyr());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Creatures lose forestwalk when Lumbering Satyr leaves")
    void forestwalkIsLostWhenLumberingSatyrLeaves() {
        harness.addToBattlefield(player1, new LumberingSatyr());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Lumbering Satyr"));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FORESTWALK)).isFalse();
    }
}
