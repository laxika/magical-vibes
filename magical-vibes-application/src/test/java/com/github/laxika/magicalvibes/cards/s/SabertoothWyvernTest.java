package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SabertoothWyvernTest extends BaseCardTest {

    @Test
    @DisplayName("Has flying and first strike on the battlefield")
    void hasFlyingAndFirstStrike() {
        harness.addToBattlefield(player1, new SabertoothWyvern());

        Permanent wyvern = findPermanent(player1, "Sabertooth Wyvern");

        assertThat(gqs.hasKeyword(gd, wyvern, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, wyvern, Keyword.FIRST_STRIKE)).isTrue();
    }
}
