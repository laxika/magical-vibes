package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

class WoollyMammothsTest extends BaseCardTest {

    private Permanent mammoths() {
        Permanent mammoths = new Permanent(new WoollyMammoths());
        mammoths.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(mammoths);
        return mammoths;
    }

    private void addSnowLand(com.github.laxika.magicalvibes.model.Player player) {
        Permanent snowLand = new Permanent(new Plains());
        TestCards.mutableCard(snowLand).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        gd.playerBattlefields.get(player.getId()).add(snowLand);
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
    @DisplayName("Non-snow land does not grant trample")
    void nonSnowLandDoesNotGrantTrample() {
        Permanent mammoths = mammoths();
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new Plains()));

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
