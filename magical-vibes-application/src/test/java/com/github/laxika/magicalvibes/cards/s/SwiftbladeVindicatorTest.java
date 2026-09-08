package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwiftbladeVindicatorTest extends BaseCardTest {

    @Test
    void hasVigilanceTrampleAndDoubleStrikeOnTheBattlefield() {
        harness.addToBattlefield(player1, new SwiftbladeVindicator());

        Permanent vindicator = findPermanent(player1, "Swiftblade Vindicator");

        assertThat(gqs.hasKeyword(gd, vindicator, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, vindicator, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, vindicator, Keyword.DOUBLE_STRIKE)).isTrue();
    }
}
