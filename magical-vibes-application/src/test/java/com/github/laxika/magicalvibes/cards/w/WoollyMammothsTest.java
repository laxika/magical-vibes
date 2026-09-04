package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WoollyMammoths.class, Plains.class, SnowCoveredPlains.class})
class WoollyMammothsTest extends BaseCardTest {

    private Permanent mammoths() {
        return addCreatureReady(player1, new WoollyMammoths());
    }

    private void addSnowLand(com.github.laxika.magicalvibes.model.Player player) {
        harness.addToBattlefield(player, new SnowCoveredPlains());
    }

    @Test
    @DisplayName("Has trample while you control a snow land")
    void hasTrampleWithSnowLand() {
        Permanent mammoths = mammoths();
        addSnowLand(player1);

        assertThat(gqs.hasKeyword(gd, mammoths, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("No trample without a snow land")
    void noTrampleWithoutSnowLand() {
        Permanent mammoths = mammoths();

        assertThat(gqs.hasKeyword(gd, mammoths, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Losing your snow land removes trample")
    void losingSnowLandRemovesTrample() {
        Permanent mammoths = mammoths();
        Permanent snowLand = harness.addToBattlefieldAndReturn(player1, new SnowCoveredPlains());

        assertThat(gqs.hasKeyword(gd, mammoths, Keyword.TRAMPLE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(snowLand);

        assertThat(gqs.hasKeyword(gd, mammoths, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Non-snow land does not grant trample")
    void nonSnowLandDoesNotGrantTrample() {
        Permanent mammoths = mammoths();
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.hasKeyword(gd, mammoths, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Opponent's snow land does not grant trample")
    void opponentSnowLandDoesNotGrantTrample() {
        Permanent mammoths = mammoths();
        addSnowLand(player2);

        assertThat(gqs.hasKeyword(gd, mammoths, Keyword.TRAMPLE)).isFalse();
    }
}
