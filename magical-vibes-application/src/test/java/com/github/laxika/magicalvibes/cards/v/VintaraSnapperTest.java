package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VintaraSnapper.class, RhysticCave.class})
class VintaraSnapperTest extends BaseCardTest {

    @Test
    @DisplayName("Vintara Snapper has shroud when its controller controls no untapped lands")
    void hasShroudWithNoUntappedLands() {
        Permanent snapper = harness.addToBattlefieldAndReturn(player1, new VintaraSnapper());

        assertThat(gqs.hasKeyword(gd, snapper, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Vintara Snapper loses shroud while its controller controls an untapped land")
    void losesShroudWithUntappedLand() {
        Permanent snapper = harness.addToBattlefieldAndReturn(player1, new VintaraSnapper());
        harness.addToBattlefield(player1, new RhysticCave());

        assertThat(gqs.hasKeyword(gd, snapper, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Vintara Snapper has shroud when all lands its controller controls are tapped")
    void hasShroudWithOnlyTappedLands() {
        Permanent snapper = harness.addToBattlefieldAndReturn(player1, new VintaraSnapper());
        Permanent cave = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        cave.tap();

        assertThat(gqs.hasKeyword(gd, snapper, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Vintara Snapper loses shroud when a controlled land becomes untapped")
    void losesShroudWhenLandBecomesUntapped() {
        Permanent snapper = harness.addToBattlefieldAndReturn(player1, new VintaraSnapper());
        Permanent cave = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        cave.tap();

        assertThat(gqs.hasKeyword(gd, snapper, Keyword.SHROUD)).isTrue();

        cave.untap();

        assertThat(gqs.hasKeyword(gd, snapper, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Vintara Snapper ignores untapped lands controlled by an opponent")
    void ignoresOpponentsUntappedLand() {
        Permanent snapper = harness.addToBattlefieldAndReturn(player1, new VintaraSnapper());
        harness.addToBattlefield(player2, new RhysticCave());

        assertThat(gqs.hasKeyword(gd, snapper, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Vintara Snapper ignores untapped nonland permanents")
    void ignoresUntappedNonlandPermanents() {
        Permanent snapper = harness.addToBattlefieldAndReturn(player1, new VintaraSnapper());
        harness.addToBattlefield(player1, new VintaraSnapper());

        assertThat(gqs.hasKeyword(gd, snapper, Keyword.SHROUD)).isTrue();
    }
}
