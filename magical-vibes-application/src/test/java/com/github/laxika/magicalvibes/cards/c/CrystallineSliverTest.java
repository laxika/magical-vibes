package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CrystallineSliverTest extends BaseCardTest {

    @Test
    @DisplayName("All Slivers have shroud, including Crystalline Sliver and opposing Slivers")
    void grantsShroudToAllSlivers() {
        harness.addToBattlefield(player1, new CrystallineSliver());
        harness.addToBattlefield(player1, new MetallicSliver());
        harness.addToBattlefield(player2, new MetallicSliver());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent crystallineSliver = findPermanent(player1, "Crystalline Sliver");
        Permanent ownSliver = findPermanent(player1, "Metallic Sliver");
        Permanent opposingSliver = findPermanent(player2, "Metallic Sliver");
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.hasKeyword(gd, crystallineSliver, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownSliver, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingSliver, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Slivers lose granted shroud when Crystalline Sliver leaves the battlefield")
    void grantedShroudIsRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new CrystallineSliver());
        harness.addToBattlefield(player1, new MetallicSliver());

        Permanent sliver = findPermanent(player1, "Metallic Sliver");
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.SHROUD)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Crystalline Sliver"));

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.SHROUD)).isFalse();
    }
}
