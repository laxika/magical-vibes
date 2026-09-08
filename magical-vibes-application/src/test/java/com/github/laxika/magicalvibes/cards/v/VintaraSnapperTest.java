package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VintaraSnapperTest extends BaseCardTest {

    @Test
    @DisplayName("Vintara Snapper has shroud when its controller controls no untapped lands")
    void hasShroudWithNoUntappedLands() {
        harness.addToBattlefield(player1, new VintaraSnapper());

        Permanent snapper = findPermanent(player1, "Vintara Snapper");

        assertThat(gqs.hasKeyword(gd, snapper, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Vintara Snapper loses shroud while its controller controls an untapped land")
    void losesShroudWithUntappedLand() {
        harness.addToBattlefield(player1, new VintaraSnapper());
        harness.addToBattlefield(player1, new Forest());

        Permanent snapper = findPermanent(player1, "Vintara Snapper");

        assertThat(gqs.hasKeyword(gd, snapper, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Vintara Snapper has shroud when all lands its controller controls are tapped")
    void hasShroudWithOnlyTappedLands() {
        harness.addToBattlefield(player1, new VintaraSnapper());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        Permanent snapper = findPermanent(player1, "Vintara Snapper");

        assertThat(gqs.hasKeyword(gd, snapper, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Vintara Snapper loses shroud when a controlled land becomes untapped")
    void losesShroudWhenLandBecomesUntapped() {
        harness.addToBattlefield(player1, new VintaraSnapper());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        Permanent snapper = findPermanent(player1, "Vintara Snapper");
        assertThat(gqs.hasKeyword(gd, snapper, Keyword.SHROUD)).isTrue();

        forest.untap();

        assertThat(gqs.hasKeyword(gd, snapper, Keyword.SHROUD)).isFalse();
    }
}
