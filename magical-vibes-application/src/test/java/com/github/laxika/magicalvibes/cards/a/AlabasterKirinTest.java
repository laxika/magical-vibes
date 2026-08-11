package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlabasterKirinTest extends BaseCardTest {

    @Test
    @DisplayName("Has flying and vigilance on the battlefield")
    void hasFlyingAndVigilance() {
        harness.addToBattlefield(player1, new AlabasterKirin());

        Permanent kirin = findPermanent(player1, "Alabaster Kirin");

        assertThat(gqs.hasKeyword(gd, kirin, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, kirin, Keyword.VIGILANCE)).isTrue();
    }
}
